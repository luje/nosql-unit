package com.lordofthejars.nosqlunit.core.integration;

import com.lordofthejars.nosqlunit.core.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.runners.model.Statement;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsArrayWithSize.arrayWithSize;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class WhenTestClassIsAnnotatedWithNoSQLUnitAnnotationsJunit5 {

	@Mock
	public Statement base;

	@Mock
	public LoadStrategyFactory loadStrategyFactory;

	@Mock
	public DatabaseOperation databaseOperation;

	@Mock
	public AbstractCustomizableDatabaseOperation abstractCustomizableDatabaseOperation;
	
	@Mock
	public LoadStrategyOperation loadStrategyOperation;

	@Mock
	public InjectAnnotationProcessor injectAnnotationProcessor;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void selective_annotations_should_not_load_data_of_not_identified_rules_but_global() throws Throwable {
		when(loadStrategyFactory.getLoadStrategyInstance(LoadStrategyEnum.INSERT, databaseOperation)).thenReturn(
				loadStrategyOperation);

		Method frameworkMethod = frameworkMethod(MyGlobalAndSelectiveClass.class, "my_unknown_test");

		AbstractNoSqlTestExtension abstractNoSqlTestRule = mock(AbstractNoSqlTestExtension.class, Mockito.CALLS_REAL_METHODS);
		abstractNoSqlTestRule.setIdentifier("two");

		doReturn(databaseOperation).when(abstractNoSqlTestRule).getDatabaseOperation();
		doReturn("json").when(abstractNoSqlTestRule).getWorkingExtension();

		when(abstractNoSqlTestRule.getDatabaseOperation()).thenReturn(databaseOperation);

		abstractNoSqlTestRule.setLoadStrategyFactory(loadStrategyFactory);
		abstractNoSqlTestRule.setInjectAnnotationProcessor(injectAnnotationProcessor);

        ExtensionContext context = extensionContext(new MyGlobalAndSelectiveClass(), MyGlobalAndSelectiveClass.class, frameworkMethod);

        abstractNoSqlTestRule.beforeEach(context);
		abstractNoSqlTestRule.afterEach(context);

		ArgumentCaptor<InputStream[]> streamCaptor = ArgumentCaptor.forClass(InputStream[].class);

		verify(loadStrategyOperation, times(1)).executeScripts(streamCaptor.capture());

		InputStream[] isContents = streamCaptor.getValue();
		String scriptContent = IOUtils.readFullStream(isContents[0]);
		assertThat(scriptContent, is("Class Annotation"));

	}

    @Test
	public void selective_annotations_should_load_only_load_data_of_identified_rules_and_global_data() throws Throwable {
		when(loadStrategyFactory.getLoadStrategyInstance(LoadStrategyEnum.INSERT, databaseOperation)).thenReturn(
				loadStrategyOperation);

		Method frameworkMethod = frameworkMethod(MyGlobalAndSelectiveClass.class, "my_unknown_test");

		AbstractNoSqlTestExtension abstractNoSqlTestRule = mock(AbstractNoSqlTestExtension.class, Mockito.CALLS_REAL_METHODS);
		abstractNoSqlTestRule.setIdentifier("one");

		doReturn(databaseOperation).when(abstractNoSqlTestRule).getDatabaseOperation();
		doReturn("json").when(abstractNoSqlTestRule).getWorkingExtension();

		when(abstractNoSqlTestRule.getDatabaseOperation()).thenReturn(databaseOperation);

		abstractNoSqlTestRule.setLoadStrategyFactory(loadStrategyFactory);
		abstractNoSqlTestRule.setInjectAnnotationProcessor(injectAnnotationProcessor);

        ExtensionContext context = extensionContext(new MyGlobalAndSelectiveClass(), MyGlobalAndSelectiveClass.class, frameworkMethod);

        abstractNoSqlTestRule.beforeEach(context);
        abstractNoSqlTestRule.afterEach(context);

		ArgumentCaptor<InputStream[]> streamCaptor = ArgumentCaptor.forClass(InputStream[].class);

		verify(loadStrategyOperation, times(1)).executeScripts(streamCaptor.capture());

		InputStream[] isContents = streamCaptor.getValue();
		String scriptContent = IOUtils.readFullStream(isContents[0]);
		assertThat(scriptContent, is("Class Annotation"));

		scriptContent = IOUtils.readFullStream(isContents[1]);
		assertThat(scriptContent, is("Selective Annotation"));
	}

	@Test
	public void selective_annotations_should_not_load_data_of_not_identified_rules() throws Throwable {
		when(loadStrategyFactory.getLoadStrategyInstance(LoadStrategyEnum.INSERT, databaseOperation)).thenReturn(
				loadStrategyOperation);

		Method frameworkMethod = frameworkMethod(MySelectiveClass.class, "my_unknown_test");

		AbstractNoSqlTestExtension abstractNoSqlTestRule = mock(AbstractNoSqlTestExtension.class, Mockito.CALLS_REAL_METHODS);
		abstractNoSqlTestRule.setIdentifier("two");

		doReturn(databaseOperation).when(abstractNoSqlTestRule).getDatabaseOperation();
		doReturn("json").when(abstractNoSqlTestRule).getWorkingExtension();

		when(abstractNoSqlTestRule.getDatabaseOperation()).thenReturn(databaseOperation);

		abstractNoSqlTestRule.setLoadStrategyFactory(loadStrategyFactory);
		abstractNoSqlTestRule.setInjectAnnotationProcessor(injectAnnotationProcessor);

		ArgumentCaptor<InputStream[]> streamCaptor = ArgumentCaptor.forClass(InputStream[].class);

        ExtensionContext context = extensionContext(new MySelectiveClass(), MySelectiveClass.class, frameworkMethod);

        abstractNoSqlTestRule.beforeEach(context);
        abstractNoSqlTestRule.afterEach(context);

		verify(loadStrategyOperation, times(1)).executeScripts(streamCaptor.capture());

		assertThat(streamCaptor.getValue(), arrayWithSize(0));

	}

	@Test
	public void selective_annotations_should_load_only_load_data_of_identified_rules() throws Throwable {
		when(loadStrategyFactory.getLoadStrategyInstance(LoadStrategyEnum.INSERT, databaseOperation)).thenReturn(
				loadStrategyOperation);

		Method frameworkMethod = frameworkMethod(MySelectiveClass.class, "my_unknown_test");

		AbstractNoSqlTestExtension abstractNoSqlTestRule = mock(AbstractNoSqlTestExtension.class, Mockito.CALLS_REAL_METHODS);
		abstractNoSqlTestRule.setIdentifier("one");

		doReturn(databaseOperation).when(abstractNoSqlTestRule).getDatabaseOperation();
		doReturn("json").when(abstractNoSqlTestRule).getWorkingExtension();

		when(abstractNoSqlTestRule.getDatabaseOperation()).thenReturn(databaseOperation);

		abstractNoSqlTestRule.setLoadStrategyFactory(loadStrategyFactory);
		abstractNoSqlTestRule.setInjectAnnotationProcessor(injectAnnotationProcessor);

        ExtensionContext context = extensionContext(new MySelectiveClass(), MySelectiveClass.class, frameworkMethod);

        abstractNoSqlTestRule.beforeEach(context);
        abstractNoSqlTestRule.afterEach(context);

		ArgumentCaptor<InputStream[]> streamCaptor = ArgumentCaptor.forClass(InputStream[].class);

		verify(loadStrategyOperation, times(1)).executeScripts(streamCaptor.capture());

		InputStream[] isContents = streamCaptor.getValue();
		String scriptContent = IOUtils.readFullStream(isContents[0]);
		assertThat(scriptContent, is("Selective Annotation"));

	}

	
	
	@Test
	public void annotated_class_without_locations_should_use_class_name_approach() throws Throwable {

		when(loadStrategyFactory.getLoadStrategyInstance(LoadStrategyEnum.INSERT, databaseOperation)).thenReturn(
				loadStrategyOperation);

		Method frameworkMethod = frameworkMethod(MyTestClass.class, "my_unknown_test");

		AbstractNoSqlTestExtension abstractNoSqlTestRule = mock(AbstractNoSqlTestExtension.class, Mockito.CALLS_REAL_METHODS);

		doReturn(databaseOperation).when(abstractNoSqlTestRule).getDatabaseOperation();
		doReturn("json").when(abstractNoSqlTestRule).getWorkingExtension();

		when(abstractNoSqlTestRule.getDatabaseOperation()).thenReturn(databaseOperation);

		abstractNoSqlTestRule.setLoadStrategyFactory(loadStrategyFactory);
		abstractNoSqlTestRule.setInjectAnnotationProcessor(injectAnnotationProcessor);

        ExtensionContext context = extensionContext(new MyTestClass(), MyTestClass.class, frameworkMethod);

        abstractNoSqlTestRule.beforeEach(context);
        abstractNoSqlTestRule.afterEach(context);

		ArgumentCaptor<InputStream[]> streamsCaptor = ArgumentCaptor.forClass(InputStream[].class);
		ArgumentCaptor<InputStream> streamCaptor = ArgumentCaptor.forClass(InputStream.class);

		verify(loadStrategyOperation, times(1)).executeScripts(streamsCaptor.capture());

		InputStream[] isContents = streamsCaptor.getValue();
		String scriptContent = IOUtils.readFullStream(isContents[0]);
		assertThat(scriptContent, is("Default Class Name Strategy 2"));

		verify(databaseOperation, times(1)).databaseIs(streamCaptor.capture());

		InputStream isContent = streamCaptor.getValue();
		scriptContent = IOUtils.readFullStream(isContent);
		assertThat(scriptContent, is("Default Class Name Strategy 2"));

	}

	@Test
	public void annotated_methods_without_locations_should_use_class_name_approach_if_method_file_not_found()
			throws Throwable {

		when(loadStrategyFactory.getLoadStrategyInstance(LoadStrategyEnum.INSERT, databaseOperation)).thenReturn(
				loadStrategyOperation);

		Method frameworkMethod = frameworkMethod(MyTestMethodClass.class, "my_unknown_test");

		AbstractNoSqlTestExtension abstractNoSqlTestRule = mock(AbstractNoSqlTestExtension.class, Mockito.CALLS_REAL_METHODS);

		doReturn("json").when(abstractNoSqlTestRule).getWorkingExtension();
		doReturn(databaseOperation).when(abstractNoSqlTestRule).getDatabaseOperation();
		when(abstractNoSqlTestRule.getDatabaseOperation()).thenReturn(databaseOperation);

		abstractNoSqlTestRule.setLoadStrategyFactory(loadStrategyFactory);
		abstractNoSqlTestRule.setInjectAnnotationProcessor(injectAnnotationProcessor);

        ExtensionContext context = extensionContext(new MyTestMethodClass(), MyTestMethodClass.class, frameworkMethod);

        abstractNoSqlTestRule.beforeEach(context);
        abstractNoSqlTestRule.afterEach(context);

		ArgumentCaptor<InputStream[]> streamsCaptor = ArgumentCaptor.forClass(InputStream[].class);
		ArgumentCaptor<InputStream> streamCaptor = ArgumentCaptor.forClass(InputStream.class);

		verify(loadStrategyOperation, times(1)).executeScripts(streamsCaptor.capture());

		InputStream[] isContents = streamsCaptor.getValue();
		String scriptContent = IOUtils.readFullStream(isContents[0]);
		assertThat(scriptContent, is("Default Class Name Strategy"));

		verify(databaseOperation, times(1)).databaseIs(streamCaptor.capture());

		InputStream isContent = streamCaptor.getValue();
		scriptContent = IOUtils.readFullStream(isContent);
		assertThat(scriptContent, is("Default Class Name Strategy"));

	}

	@Test
	public void customized_comparision_test_classes_should_insert_data_using_customized_approach() throws Throwable {
		
		when(loadStrategyFactory.getLoadStrategyInstance(LoadStrategyEnum.INSERT, abstractCustomizableDatabaseOperation)).thenReturn(
				loadStrategyOperation);
		
		Method frameworkMethod = frameworkMethod(MyTestWithCustomComparisionStrategy.class, "my_unknown_test");
		AbstractNoSqlTestExtension abstractNoSqlTestRule = mock(AbstractNoSqlTestExtension.class, Mockito.CALLS_REAL_METHODS);
		
		doReturn("json").when(abstractNoSqlTestRule).getWorkingExtension();
		doReturn(abstractCustomizableDatabaseOperation).when(abstractNoSqlTestRule).getDatabaseOperation();
		when(abstractNoSqlTestRule.getDatabaseOperation()).thenReturn(abstractCustomizableDatabaseOperation);
		
		abstractNoSqlTestRule.setLoadStrategyFactory(loadStrategyFactory);
		abstractNoSqlTestRule.setInjectAnnotationProcessor(injectAnnotationProcessor);

        ExtensionContext context = extensionContext(new MyTestWithCustomComparisionStrategy(), MyTestWithCustomComparisionStrategy.class, frameworkMethod);

        abstractNoSqlTestRule.beforeEach(context);
        abstractNoSqlTestRule.afterEach(context);
		
		verify(abstractCustomizableDatabaseOperation, times(1)).setComparisonStrategy(any(ComparisonStrategy.class));
		
	}
	
	@Test
	public void customized_insertation_test_classes_should_insert_data_using_customized_approach() throws Throwable {
		
		when(loadStrategyFactory.getLoadStrategyInstance(LoadStrategyEnum.INSERT, abstractCustomizableDatabaseOperation)).thenReturn(
				loadStrategyOperation);
		
		Method frameworkMethod = frameworkMethod(MyTestWithCustomInsertStrategy.class, "my_unknown_test");
		AbstractNoSqlTestExtension abstractNoSqlTestRule = mock(AbstractNoSqlTestExtension.class, Mockito.CALLS_REAL_METHODS);
		
		doReturn("json").when(abstractNoSqlTestRule).getWorkingExtension();
		doReturn(abstractCustomizableDatabaseOperation).when(abstractNoSqlTestRule).getDatabaseOperation();
		when(abstractNoSqlTestRule.getDatabaseOperation()).thenReturn(abstractCustomizableDatabaseOperation);
		
		abstractNoSqlTestRule.setLoadStrategyFactory(loadStrategyFactory);
		abstractNoSqlTestRule.setInjectAnnotationProcessor(injectAnnotationProcessor);

        ExtensionContext context = extensionContext(new MyTestWithCustomInsertStrategy(), MyTestWithCustomInsertStrategy.class, frameworkMethod);

        abstractNoSqlTestRule.beforeEach(context);
        abstractNoSqlTestRule.afterEach(context);
		
		verify(abstractCustomizableDatabaseOperation, times(1)).setInsertionStrategy(any(InsertionStrategy.class));
		
	}
	
	@Test
	public void annotated_methods_without_locations_should_use_method_name_approach() throws Throwable {

		when(loadStrategyFactory.getLoadStrategyInstance(LoadStrategyEnum.INSERT, databaseOperation)).thenReturn(
				loadStrategyOperation);

		Method frameworkMethod = frameworkMethod(MyTestMethodClass.class, "my_method_test");

		AbstractNoSqlTestExtension abstractNoSqlTestRule = mock(AbstractNoSqlTestExtension.class, Mockito.CALLS_REAL_METHODS);

		doReturn("json").when(abstractNoSqlTestRule).getWorkingExtension();
		doReturn(databaseOperation).when(abstractNoSqlTestRule).getDatabaseOperation();
		when(abstractNoSqlTestRule.getDatabaseOperation()).thenReturn(databaseOperation);

		abstractNoSqlTestRule.setLoadStrategyFactory(loadStrategyFactory);
		abstractNoSqlTestRule.setInjectAnnotationProcessor(injectAnnotationProcessor);

        ExtensionContext context = extensionContext(new MyTestMethodClass(), MyTestMethodClass.class, frameworkMethod);

        abstractNoSqlTestRule.beforeEach(context);
        abstractNoSqlTestRule.afterEach(context);

		ArgumentCaptor<InputStream[]> streamsCaptor = ArgumentCaptor.forClass(InputStream[].class);
		ArgumentCaptor<InputStream> streamCaptor = ArgumentCaptor.forClass(InputStream.class);

		verify(loadStrategyOperation, times(1)).executeScripts(streamsCaptor.capture());

		InputStream[] isContents = streamsCaptor.getValue();
		String scriptContent = IOUtils.readFullStream(isContents[0]);
		assertThat(scriptContent, is("Default Method Name Strategy"));

		verify(databaseOperation, times(1)).databaseIs(streamCaptor.capture());

		InputStream isContent = streamCaptor.getValue();
		scriptContent = IOUtils.readFullStream(isContent);
		assertThat(scriptContent, is("Default Method Name Strategy"));

	}

	@Test
	public void annotated_methods_should_have_precedence_over_annotated_class() throws Throwable {

		when(loadStrategyFactory.getLoadStrategyInstance(LoadStrategyEnum.INSERT, databaseOperation)).thenReturn(
				loadStrategyOperation);

		Method frameworkMethod = frameworkMethod(DefaultClass.class, "my_method_test");

		AbstractNoSqlTestExtension abstractNoSqlTestRule = mock(AbstractNoSqlTestExtension.class, Mockito.CALLS_REAL_METHODS);

		doReturn(databaseOperation).when(abstractNoSqlTestRule).getDatabaseOperation();
		when(abstractNoSqlTestRule.getDatabaseOperation()).thenReturn(databaseOperation);

		abstractNoSqlTestRule.setLoadStrategyFactory(loadStrategyFactory);
		abstractNoSqlTestRule.setInjectAnnotationProcessor(injectAnnotationProcessor);

        ExtensionContext context = extensionContext(new DefaultClass(), DefaultClass.class, frameworkMethod);

        abstractNoSqlTestRule.beforeEach(context);
        abstractNoSqlTestRule.afterEach(context);

		ArgumentCaptor<InputStream[]> streamsCaptor = ArgumentCaptor.forClass(InputStream[].class);
		ArgumentCaptor<InputStream> streamCaptor = ArgumentCaptor.forClass(InputStream.class);

		verify(loadStrategyOperation, times(1)).executeScripts(streamsCaptor.capture());

		InputStream[] isContents = streamsCaptor.getValue();
		String scriptContent = IOUtils.readFullStream(isContents[0]);
		assertThat(scriptContent, is("Method Annotation"));

		verify(databaseOperation, times(1)).databaseIs(streamCaptor.capture());

		InputStream isContent = streamCaptor.getValue();
		scriptContent = IOUtils.readFullStream(isContent);
		assertThat(scriptContent, is("Method Annotation"));

	}

	@Test
	public void class_annotation_should_be_used_if_no_annotated_methods() throws Throwable {

		when(loadStrategyFactory.getLoadStrategyInstance(LoadStrategyEnum.INSERT, databaseOperation)).thenReturn(
				loadStrategyOperation);

		Method frameworkMethod = frameworkMethod(DefaultClass.class, "my_unknown_test_2");

		AbstractNoSqlTestExtension abstractNoSqlTestRule = mock(AbstractNoSqlTestExtension.class, Mockito.CALLS_REAL_METHODS);

		doReturn(databaseOperation).when(abstractNoSqlTestRule).getDatabaseOperation();
		when(abstractNoSqlTestRule.getDatabaseOperation()).thenReturn(databaseOperation);

		abstractNoSqlTestRule.setLoadStrategyFactory(loadStrategyFactory);
		abstractNoSqlTestRule.setInjectAnnotationProcessor(injectAnnotationProcessor);

        ExtensionContext context = extensionContext(new DefaultClass(), DefaultClass.class, frameworkMethod);

        abstractNoSqlTestRule.beforeEach(context);
        abstractNoSqlTestRule.afterEach(context);

		ArgumentCaptor<InputStream[]> streamsCaptor = ArgumentCaptor.forClass(InputStream[].class);
		ArgumentCaptor<InputStream> streamCaptor = ArgumentCaptor.forClass(InputStream.class);

		verify(loadStrategyOperation, times(1)).executeScripts(streamsCaptor.capture());

		InputStream[] isContents = streamsCaptor.getValue();
		String scriptContent = IOUtils.readFullStream(isContents[0]);
		assertThat(scriptContent, is("Class Annotation"));

		verify(databaseOperation, times(1)).databaseIs(streamCaptor.capture());

		InputStream isContent = streamCaptor.getValue();
		scriptContent = IOUtils.readFullStream(isContent);
		assertThat(scriptContent, is("Method Annotation"));

	}

	@Test
	public void class_annotation_should_be_used_if_any_annotated_methods() throws Throwable {

		when(loadStrategyFactory.getLoadStrategyInstance(LoadStrategyEnum.INSERT, databaseOperation)).thenReturn(
				loadStrategyOperation);

		Method frameworkMethod = frameworkMethod(DefaultClass.class, "my_unknown_test");

		AbstractNoSqlTestExtension abstractNoSqlTestRule = mock(AbstractNoSqlTestExtension.class, Mockito.CALLS_REAL_METHODS);

		doReturn(databaseOperation).when(abstractNoSqlTestRule).getDatabaseOperation();
		when(abstractNoSqlTestRule.getDatabaseOperation()).thenReturn(databaseOperation);

		abstractNoSqlTestRule.setLoadStrategyFactory(loadStrategyFactory);
		abstractNoSqlTestRule.setInjectAnnotationProcessor(injectAnnotationProcessor);

        ExtensionContext context = extensionContext(new DefaultClass(), DefaultClass.class, frameworkMethod);

        abstractNoSqlTestRule.beforeEach(context);
        abstractNoSqlTestRule.afterEach(context);

		ArgumentCaptor<InputStream[]> streamsCaptor = ArgumentCaptor.forClass(InputStream[].class);
		ArgumentCaptor<InputStream> streamCaptor = ArgumentCaptor.forClass(InputStream.class);

		verify(loadStrategyOperation, times(1)).executeScripts(streamsCaptor.capture());

		InputStream[] isContents = streamsCaptor.getValue();
		String scriptContent = IOUtils.readFullStream(isContents[0]);
		assertThat(scriptContent, is("Class Annotation"));

		verify(databaseOperation, times(1)).databaseIs(streamCaptor.capture());

		InputStream isContent = streamCaptor.getValue();
		scriptContent = IOUtils.readFullStream(isContent);
		assertThat(scriptContent, is("Class Annotation"));

	}

	@Test
	public void selective_matchers_annotation_should_only_verify_identified_connection() throws Throwable {

		when(loadStrategyFactory.getLoadStrategyInstance(LoadStrategyEnum.INSERT, databaseOperation)).thenReturn(
				loadStrategyOperation);

		Method frameworkMethod = frameworkMethod(SelectiveDefaultClass.class, "my_unknown_test");

		AbstractNoSqlTestExtension abstractNoSqlTestRule = mock(AbstractNoSqlTestExtension.class, Mockito.CALLS_REAL_METHODS);

		doReturn(databaseOperation).when(abstractNoSqlTestRule).getDatabaseOperation();
		abstractNoSqlTestRule.setIdentifier("one");

		when(abstractNoSqlTestRule.getDatabaseOperation()).thenReturn(databaseOperation);

		abstractNoSqlTestRule.setLoadStrategyFactory(loadStrategyFactory);
		abstractNoSqlTestRule.setInjectAnnotationProcessor(injectAnnotationProcessor);

        ExtensionContext context = extensionContext(new SelectiveDefaultClass(), SelectiveDefaultClass.class, frameworkMethod);

        abstractNoSqlTestRule.beforeEach(context);
        abstractNoSqlTestRule.afterEach(context);

		ArgumentCaptor<InputStream> streamCaptor = ArgumentCaptor.forClass(InputStream.class);

		verify(databaseOperation, times(1)).databaseIs(streamCaptor.capture());

		InputStream isContent = streamCaptor.getValue();
		String scriptContent = IOUtils.readFullStream(isContent);
		assertThat(scriptContent, is("Selective Annotation"));

	}

	@Test(expected = IllegalArgumentException.class)
	public void selective_matchers_annotation_should_fail_if_unknown_identified_connection() throws Throwable {

		when(loadStrategyFactory.getLoadStrategyInstance(LoadStrategyEnum.INSERT, databaseOperation)).thenReturn(
				loadStrategyOperation);

		Method frameworkMethod = frameworkMethod(SelectiveDefaultClass.class, "my_unknown_test");

		AbstractNoSqlTestExtension abstractNoSqlTestRule = mock(AbstractNoSqlTestExtension.class, Mockito.CALLS_REAL_METHODS);

		doReturn("json").when(abstractNoSqlTestRule).getWorkingExtension();
		doReturn(databaseOperation).when(abstractNoSqlTestRule).getDatabaseOperation();
		abstractNoSqlTestRule.setIdentifier("two");

		when(abstractNoSqlTestRule.getDatabaseOperation()).thenReturn(databaseOperation);

		abstractNoSqlTestRule.setLoadStrategyFactory(loadStrategyFactory);
		abstractNoSqlTestRule.setInjectAnnotationProcessor(injectAnnotationProcessor);

        ExtensionContext context = extensionContext(new SelectiveDefaultClass(), SelectiveDefaultClass.class, frameworkMethod);

        abstractNoSqlTestRule.beforeEach(context);
        abstractNoSqlTestRule.afterEach(context);
		fail();

	}

	@Test
	public void global_location_should_have_precedence_over_selective_matchers() throws Throwable {

		when(loadStrategyFactory.getLoadStrategyInstance(LoadStrategyEnum.INSERT, databaseOperation)).thenReturn(
				loadStrategyOperation);

		Method frameworkMethod = frameworkMethod(SelectiveAndLocationClass.class, "my_unknown_test");

		AbstractNoSqlTestExtension abstractNoSqlTestRule = mock(AbstractNoSqlTestExtension.class, Mockito.CALLS_REAL_METHODS);

		doReturn(databaseOperation).when(abstractNoSqlTestRule).getDatabaseOperation();
		abstractNoSqlTestRule.setIdentifier("one");

		when(abstractNoSqlTestRule.getDatabaseOperation()).thenReturn(databaseOperation);

		abstractNoSqlTestRule.setLoadStrategyFactory(loadStrategyFactory);
		abstractNoSqlTestRule.setInjectAnnotationProcessor(injectAnnotationProcessor);

        ExtensionContext context = extensionContext(new SelectiveAndLocationClass(), SelectiveAndLocationClass.class, frameworkMethod);

        abstractNoSqlTestRule.beforeEach(context);
        abstractNoSqlTestRule.afterEach(context);

		ArgumentCaptor<InputStream> streamCaptor = ArgumentCaptor.forClass(InputStream.class);

		verify(databaseOperation, times(1)).databaseIs(streamCaptor.capture());

		InputStream isContent = streamCaptor.getValue();
		String scriptContent = IOUtils.readFullStream(isContent);
		assertThat(scriptContent, is("Class Annotation"));

	}

	@Test
	public void not_valid_locations_should_throw_an_exception() throws Throwable {

		when(loadStrategyFactory.getLoadStrategyInstance(LoadStrategyEnum.INSERT, databaseOperation)).thenReturn(
				loadStrategyOperation);

		Method frameworkMethod = frameworkMethod(MyUknownClass.class, "my_unknown_test");

		AbstractNoSqlTestExtension abstractNoSqlTestRule = mock(AbstractNoSqlTestExtension.class, Mockito.CALLS_REAL_METHODS);

		doReturn(databaseOperation).when(abstractNoSqlTestRule).getDatabaseOperation();
		doReturn("json").when(abstractNoSqlTestRule).getWorkingExtension();

		when(abstractNoSqlTestRule.getDatabaseOperation()).thenReturn(databaseOperation);

		abstractNoSqlTestRule.setLoadStrategyFactory(loadStrategyFactory);
		abstractNoSqlTestRule.setInjectAnnotationProcessor(injectAnnotationProcessor);

		try {
            ExtensionContext context = extensionContext(new MyUknownClass(), MyUknownClass.class, frameworkMethod);

            abstractNoSqlTestRule.beforeEach(context);
            abstractNoSqlTestRule.afterEach(context);
			fail();
		} catch (IllegalArgumentException e) {
			assertThat(
					e.getMessage(),
					is("File specified in locations property are not present in classpath, or no files matching default name are found. Valid default locations are: /com/lordofthejars/nosqlunit/core/integration/MyUknownClass.json or /com/lordofthejars/nosqlunit/core/integration/MyUknownClass#my_unknown_test.json"));
		}

	}

	private Method frameworkMethod(Class<?> testClass, String methodName) {

		try {
			return testClass.getMethod(methodName);
		} catch (SecurityException e) {
			throw new IllegalArgumentException(e);
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException(e);
		}

	}

    private ExtensionContext extensionContext(Object test, Class<?> testClass, Method testMethod) {
        final ExtensionContext extensionContext = mock(ExtensionContext.class);

        doReturn(Optional.ofNullable(test)).when(extensionContext).getTestInstance();
        doReturn(Optional.ofNullable(testClass)).when(extensionContext).getTestClass();
        doReturn(Optional.ofNullable(testMethod)).when(extensionContext).getTestMethod();

        return extensionContext;
    }

}
