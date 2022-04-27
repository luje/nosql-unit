package com.lordofthejars.nosqlunit.util;

import java.lang.reflect.Method;

public class DefaultClasspathLocationBuilder {

	private static final String METHOD_SEPARATOR = "#";
	
	public static final String defaultClassAnnotatedClasspathLocation(Method method) {
		String testClassName = method.getDeclaringClass().getName();
		String defaultClassAnnotatedClasspath = "/"
				+ testClassName.replace('.', '/');
		
		return defaultClassAnnotatedClasspath;
	}
	
	public static String defaultMethodAnnotatedClasspathLocation(
			Method method,
			String defaultClassAnnotatedClasspath, String suffix) {
		String testMethodName = method.getName();

		String defaultMethodAnnotatedClasspathFile = defaultClassAnnotatedClasspath
				+ METHOD_SEPARATOR
				+ testMethodName
				+ suffix;
		return defaultMethodAnnotatedClasspathFile;
	}
	
}
