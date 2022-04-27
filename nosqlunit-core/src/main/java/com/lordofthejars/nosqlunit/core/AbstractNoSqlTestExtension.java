package com.lordofthejars.nosqlunit.core;

import com.lordofthejars.nosqlunit.annotation.*;
import com.lordofthejars.nosqlunit.util.DefaultClasspathLocationBuilder;
import com.lordofthejars.nosqlunit.util.ReflectionUtil;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public abstract class AbstractNoSqlTestExtension implements BeforeEachCallback, AfterEachCallback {

    private static final String EXPECTED_RESERVED_WORD = "-expected";

    private String identifier;

    private DefaultDataSetLocationResolver defaultDataSetLocationResolver;

    private LoadStrategyFactory loadStrategyFactory = new ReflectionLoadStrategyFactory();

    private InjectAnnotationProcessor injectAnnotationProcessor;

    public AbstractNoSqlTestExtension(String identifier) {
        this.identifier = identifier;
        this.injectAnnotationProcessor = new InjectAnnotationProcessor(this.identifier);
    }

    public abstract DatabaseOperation getDatabaseOperation();

    public abstract String getWorkingExtension();

    public abstract void close();

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        final Object target = context.getTestInstance().get();
        final Class<?> testClass = context.getTestClass().get();
        final Method testMethod = context.getTestMethod().get();

        defaultDataSetLocationResolver = new DefaultDataSetLocationResolver(testClass);

        UsingDataSet usingDataSet = getUsingDataSetAnnotation(testClass, testMethod);

        if (isTestAnnotatedWithDataSet(usingDataSet)) {
            createCustomInsertationStrategyIfPresent(testClass);
            loadDataSet(usingDataSet, testMethod);
        }

        injectAnnotationProcessor.processInjectAnnotation(testClass, target, getDatabaseOperation().connectionManager());
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        final Object target = context.getTestInstance().get();
        final Class<?> testClass = context.getTestClass().get();
        final Method testMethod = context.getTestMethod().get();

        ShouldMatchDataSet shouldMatchDataSet = getShouldMatchDataSetAnnotation(testClass, testMethod);

        if (isTestAnnotatedWithExpectedDataSet(shouldMatchDataSet)) {
            createCustomComparisionStrategyIfPresent(testClass, testMethod);
            assertExpectation(shouldMatchDataSet, testMethod);
        }
    }

    private void createCustomComparisionStrategyIfPresent(Class<?> testClass, Method testMethod) {
        CustomComparisonStrategy customComparisonStrategy = getCustomComparisionStrategy(testClass);

        if (isTestAnnotatedWithCustomComparisionStrategy(customComparisonStrategy)) {
            DatabaseOperation<?> databaseOperation = getDatabaseOperation();

            if (isDatabaseOperationCustomizable(databaseOperation)) {
                Class<? extends ComparisonStrategy<?>> comparisionStrategy = customComparisonStrategy
                        .comparisonStrategy();
                ComparisonStrategy<?> comparisionStrategyObject = ReflectionUtil.createInstance(comparisionStrategy);

                Class<?> classWithAnnotation = IOUtils.getClassWithAnnotation(
                        testClass,
                        IgnorePropertyValue.class);
                if (testMethod.getAnnotation(IgnorePropertyValue.class) != null
                        || (classWithAnnotation != null && classWithAnnotation
                        .getAnnotation(
                                IgnorePropertyValue.class) != null)) {
                    comparisionStrategyObject
                            .setIgnoreProperties(getPropertiesToIgnore(testClass, testMethod));
                }

                overrideComparisionStrategy(databaseOperation,
                        comparisionStrategyObject);

            } else {
                throw new IllegalArgumentException(
                        "Custom Insertation Strategy can only be used in DatabaseOperations that extends from AbstractCustomizableDatabaseOperation");
            }
        }
    }

    private void createCustomInsertationStrategyIfPresent(Class<?> testClass) {
        CustomInsertionStrategy customInsertionStrategy = getCustomInsertationStrategy(testClass);

        if (isTestAnnotatedWithCustomInsertationStrategy(customInsertionStrategy)) {
            DatabaseOperation<?> databaseOperation = getDatabaseOperation();

            if (isDatabaseOperationCustomizable(databaseOperation)) {
                Class<? extends InsertionStrategy<?>> insertationStrategy = customInsertionStrategy
                        .insertionStrategy();
                InsertionStrategy<?> insertationStrategyObject = ReflectionUtil.createInstance(insertationStrategy);
                overrideInsertationStrategy(databaseOperation,
                        insertationStrategyObject);
            } else {
                throw new IllegalArgumentException(
                        "Custom Insertation Strategy can only be used in DatabaseOperations that extends from AbstractCustomizableDatabaseOperation");
            }

        }
    }

    private void overrideComparisionStrategy(
            DatabaseOperation<?> databaseOperation,
            ComparisonStrategy<?> comparisionStrategyObject) {
        AbstractCustomizableDatabaseOperation customizableDatabaseOperation = (AbstractCustomizableDatabaseOperation) databaseOperation;
        customizableDatabaseOperation
                .setComparisonStrategy(comparisionStrategyObject);
    }

    private void overrideInsertationStrategy(
            DatabaseOperation<?> databaseOperation,
            InsertionStrategy<?> insertationStrategyObject) {
        AbstractCustomizableDatabaseOperation customizableDatabaseOperation = (AbstractCustomizableDatabaseOperation) databaseOperation;
        customizableDatabaseOperation
                .setInsertionStrategy(insertationStrategyObject);
    }

    private boolean isDatabaseOperationCustomizable(
            DatabaseOperation databaseOperation) {
        return databaseOperation instanceof AbstractCustomizableDatabaseOperation;
    }

    private ShouldMatchDataSet getShouldMatchDataSetAnnotation(Class<?> testClass, Method method) {

        ShouldMatchDataSet shouldMatchDataSet = method
                .getAnnotation(ShouldMatchDataSet.class);

        if (!isTestAnnotatedWithExpectedDataSet(shouldMatchDataSet)) {

            Class<?> annotatedClass = IOUtils.getClassWithAnnotation(
                    testClass, ShouldMatchDataSet.class);
            shouldMatchDataSet = annotatedClass == null ? null
                    : annotatedClass
                    .getAnnotation(ShouldMatchDataSet.class);

        }

        return shouldMatchDataSet;
    }

    private String[] getPropertiesToIgnore(Class<?> testClass, Method testMethod) {
        List<String> propertyValuesToIgnore = new ArrayList<String>();

        Class<?> annotated = IOUtils.getClassWithAnnotation(testClass, IgnorePropertyValue.class);
        if (annotated != null) {
            IgnorePropertyValue annotationIgnore = annotated
                    .getAnnotation(IgnorePropertyValue.class);

            if (annotationIgnore != null) {
                String[] properties = annotationIgnore.properties();
                for (String property : properties) {
                    propertyValuesToIgnore.add(property);
                }
            }
        }

        IgnorePropertyValue ignorePropertyValue = testMethod
                .getAnnotation(IgnorePropertyValue.class);

        if (isTestAnnotatedWithIgnoreProperty(ignorePropertyValue)) {
            String[] properties = ignorePropertyValue.properties();
            for (String property : properties) {
                propertyValuesToIgnore.add(property);
            }
        }

        return propertyValuesToIgnore
                .toArray(new String[propertyValuesToIgnore.size()]);
    }

    private UsingDataSet getUsingDataSetAnnotation(Class<?> testClass, Method testMethod) {

        UsingDataSet usingDataSet = testMethod
                .getAnnotation(UsingDataSet.class);

        if (!isTestAnnotatedWithDataSet(usingDataSet)) {

            Class<?> annotatedClass = IOUtils.getClassWithAnnotation(
                    testClass, UsingDataSet.class);
            usingDataSet = annotatedClass == null ? null
                    : annotatedClass.getAnnotation(UsingDataSet.class);

        }

        return usingDataSet;
    }

    private CustomComparisonStrategy getCustomComparisionStrategy(Class<?> testClass) {
        Class<?> annotatedClass = IOUtils
                .getClassWithAnnotation(
                        testClass,
                        com.lordofthejars.nosqlunit.annotation.CustomComparisonStrategy.class);
        return annotatedClass == null ? null
                : annotatedClass
                .getAnnotation(com.lordofthejars.nosqlunit.annotation.CustomComparisonStrategy.class);
    }

    private com.lordofthejars.nosqlunit.annotation.CustomInsertionStrategy getCustomInsertationStrategy(Class<?> testClass) {

        Class<?> annotatedClass = IOUtils
                .getClassWithAnnotation(
                        testClass,
                        com.lordofthejars.nosqlunit.annotation.CustomInsertionStrategy.class);
        return annotatedClass == null ? null
                : annotatedClass
                .getAnnotation(com.lordofthejars.nosqlunit.annotation.CustomInsertionStrategy.class);
    }

    private void assertExpectation(ShouldMatchDataSet shouldMatchDataSet, Method testMethod)
            throws IOException {

        InputStream scriptContent = loadExpectedContentScript(testMethod,
                shouldMatchDataSet);

        if (isNotEmptyStream(scriptContent)) {
            getDatabaseOperation().databaseIs(scriptContent);
        } else {

            final String suffix = EXPECTED_RESERVED_WORD + "."
                    + getWorkingExtension();
            final String defaultClassLocation = DefaultClasspathLocationBuilder
                    .defaultClassAnnotatedClasspathLocation(testMethod);
            final String defaultMethodLocation = DefaultClasspathLocationBuilder
                    .defaultMethodAnnotatedClasspathLocation(testMethod,
                            defaultClassLocation, suffix);

            throw new IllegalArgumentException(
                    "File specified in location or selective matcher property "
                            + " of ShouldMatchDataSet is not present, or no files matching default location. Valid default locations are: "
                            + defaultClassLocation + suffix + " or "
                            + defaultMethodLocation);
        }

    }

    private InputStream loadExpectedContentScript(
            final Method method,
            ShouldMatchDataSet shouldMatchDataSet) throws IOException {
        String location = shouldMatchDataSet.location();
        InputStream scriptContent = null;

        if (isNotEmptyString(location)) {
            scriptContent = loadExpectedResultFromLocationAttribute(location);
        } else {

            SelectiveMatcher[] selectiveMatchers = shouldMatchDataSet
                    .withSelectiveMatcher();

            SelectiveMatcher requiredSelectiveMatcher = findSelectiveMatcherByConnectionIdentifier(selectiveMatchers);

            if (isSelectiveMatchersDefined(requiredSelectiveMatcher)) {

                scriptContent = loadExpectedResultFromLocationAttribute(requiredSelectiveMatcher
                        .location());

            } else {
                scriptContent = loadExpectedResultFromDefaultLocation(
                        method, shouldMatchDataSet);
            }
        }
        return scriptContent;
    }

    private boolean isSelectiveMatchersDefined(
            SelectiveMatcher requiredSelectiveMatcher) {
        return requiredSelectiveMatcher != null;
    }

    private SelectiveMatcher findSelectiveMatcherByConnectionIdentifier(
            SelectiveMatcher[] selectiveMatchers) {
        return Stream.of(selectiveMatchers)
                .filter(it -> Objects.equals(identifier, it.identifier()))
                .filter(it -> Objects.nonNull(it.identifier()))
                .findFirst()
                .orElse(null);
    }

    private InputStream loadExpectedResultFromDefaultLocation(
            final Method method,
            ShouldMatchDataSet shouldMatchDataSet) throws IOException {

        InputStream scriptContent = null;

        String defaultLocation = defaultDataSetLocationResolver
                .resolveDefaultDataSetLocation(shouldMatchDataSet,
                        method, EXPECTED_RESERVED_WORD + "."
                                + getWorkingExtension());

        if (defaultLocation != null) {
            scriptContent = loadExpectedResultFromLocationAttribute(defaultLocation);
        }
        return scriptContent;
    }

    private InputStream loadExpectedResultFromLocationAttribute(
            String location) throws IOException {
        InputStream scriptContent;
        scriptContent = IOUtils.getStreamFromClasspathBaseResource(
                defaultDataSetLocationResolver.getResourceBase(),
                location);
        return scriptContent;
    }

    private void loadDataSet(UsingDataSet usingDataSet,
                             Method method) throws IOException {

        List<InputStream> scriptContent = loadDatasets(usingDataSet,
                method);
        LoadStrategyEnum loadStrategyEnum = usingDataSet.loadStrategy();

        if (areDatasetsRequired(loadStrategyEnum)
                && emptyDataset(scriptContent)
                && notSelectiveAnnotation(usingDataSet
                .withSelectiveLocations())) {
            final String suffix = "." + getWorkingExtension();
            final String defaultClassLocation = DefaultClasspathLocationBuilder
                    .defaultClassAnnotatedClasspathLocation(method);
            final String defaultMethodLocation = DefaultClasspathLocationBuilder
                    .defaultMethodAnnotatedClasspathLocation(method,
                            defaultClassLocation, suffix);
            throw new IllegalArgumentException(
                    "File specified in locations property are not present in classpath, or no files matching default name are found. Valid default locations are: "
                            + defaultClassLocation
                            + suffix
                            + " or "
                            + defaultMethodLocation);
        }

        LoadStrategyOperation loadStrategyOperation = loadStrategyFactory
                .getLoadStrategyInstance(loadStrategyEnum,
                        getDatabaseOperation());
        loadStrategyOperation.executeScripts(scriptContent
                .toArray(new InputStream[scriptContent.size()]));

    }

    private boolean notSelectiveAnnotation(
            Selective[] withSelectiveLocations) {
        return withSelectiveLocations.length == 0;
    }

    private boolean emptyDataset(List<InputStream> scriptContent) {
        return scriptContent.size() == 0;
    }

    private boolean areDatasetsRequired(
            LoadStrategyEnum loadStrategyEnum) {
        return LoadStrategyEnum.DELETE_ALL != loadStrategyEnum;
    }

    private List<InputStream> loadDatasets(UsingDataSet usingDataSet,
                                           Method method) throws IOException {
        String[] locations = usingDataSet.locations();

        List<InputStream> scriptContent = new ArrayList<InputStream>();

        scriptContent.addAll(loadGlobalDataSets(usingDataSet, method,
                locations));
        scriptContent.addAll(loadSelectiveDataSets(usingDataSet));
        return scriptContent;
    }

    private List<InputStream> loadSelectiveDataSets(
            UsingDataSet usingDataSet) throws IOException {

        List<InputStream> scriptContent = new ArrayList<InputStream>();

        if (isSelectiveLocationsAttributeSpecified(usingDataSet)) {
            Selective[] selectiveLocations = usingDataSet
                    .withSelectiveLocations();
            if (selectiveLocations != null
                    && selectiveLocations.length > 0) {
                for (Selective selective : selectiveLocations) {
                    if (identifier
                            .equals(selective.identifier().trim())
                            && isLocationsAttributeSpecified(selective
                            .locations())) {
                        scriptContent
                                .addAll(IOUtils
                                        .getAllStreamsFromClasspathBaseResource(
                                                defaultDataSetLocationResolver
                                                        .getResourceBase(),
                                                selective.locations()));
                    }
                }
            }
        }

        return scriptContent;
    }

    private List<InputStream> loadGlobalDataSets(
            UsingDataSet usingDataSet, Method method,
            String[] locations) throws IOException {

        List<InputStream> scriptContent = new ArrayList<InputStream>();

        if (isLocationsAttributeSpecified(locations)) {

            scriptContent.addAll(IOUtils
                    .getAllStreamsFromClasspathBaseResource(
                            defaultDataSetLocationResolver
                                    .getResourceBase(), locations));

        } else {

            String location = defaultDataSetLocationResolver
                    .resolveDefaultDataSetLocation(usingDataSet,
                            method, "." + getWorkingExtension());

            if (location != null) {
                scriptContent.add(IOUtils
                        .getStreamFromClasspathBaseResource(
                                defaultDataSetLocationResolver
                                        .getResourceBase(), location));
            }

        }

        return scriptContent;
    }

    private boolean isSelectiveLocationsAttributeSpecified(
            UsingDataSet usingDataSet) {
        Selective[] selectiveLocations = usingDataSet
                .withSelectiveLocations();
        if (selectiveLocations != null && selectiveLocations.length > 0) {
            for (Selective selective : selectiveLocations) {
                if (identifier.equals(selective.identifier().trim())
                        && isLocationsAttributeSpecified(selective
                        .locations())) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean isNotEmptyStream(InputStream inputStream) {
        return inputStream != null;
    }

    private boolean isNotEmptyString(String location) {
        return location != null && !"".equals(location.trim());
    }

    private boolean isLocationsAttributeSpecified(String[] locations) {
        return locations != null && locations.length > 0;
    }

    private boolean isTestAnnotatedWithCustomComparisionStrategy(
            CustomComparisonStrategy customComparisonStrategy) {
        return customComparisonStrategy != null;
    }

    private boolean isTestAnnotatedWithCustomInsertationStrategy(
            CustomInsertionStrategy customInsertionStrategy) {
        return customInsertionStrategy != null;
    }

    private boolean isTestAnnotatedWithExpectedDataSet(
            ShouldMatchDataSet shouldMatchDataSet) {
        return shouldMatchDataSet != null;
    }

    private boolean isTestAnnotatedWithDataSet(UsingDataSet usingDataSet) {
        return usingDataSet != null;
    }

    private boolean isTestAnnotatedWithIgnoreProperty(
            IgnorePropertyValue ignorePropertyValue) {
        return ignorePropertyValue != null;
    }

    public void setLoadStrategyFactory(LoadStrategyFactory loadStrategyFactory) {
        this.loadStrategyFactory = loadStrategyFactory;
    }

    public void setInjectAnnotationProcessor(
            InjectAnnotationProcessor injectAnnotationProcessor) {
        this.injectAnnotationProcessor = injectAnnotationProcessor;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

}
