package com.lordofthejars.nosqlunit.util;

import org.springframework.context.ApplicationContext;

import java.util.Map;


public class SpringUtils
{
	/**
	 * Search recursively in current and parent applicationContext's for Bean of Type T
	 * @param applicationContext Spring application context to start searching from
	 * @param aClass Class type of bean to search
	 * @param <T> Type of bean to search
	 * @return first matched bean found
	 */
	public static <T> T getBeanOfType(ApplicationContext applicationContext, Class<T> aClass)
	{
		Map<String, T> beansOfType;
		do
		{
			beansOfType = applicationContext.getBeansOfType(aClass);
			if (beansOfType != null && beansOfType.size() > 0)
			{
				return beansOfType.values().stream().findFirst().orElse(null);
			}
			applicationContext = applicationContext.getParent();
		}
		while (applicationContext != null);
		return null;
	}
}
