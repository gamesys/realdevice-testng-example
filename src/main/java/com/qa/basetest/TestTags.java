package com.qa.basetest;

import com.qa.basetest.tags.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public abstract @interface TestTags {

	/**
	 * The array of test phases for which the test(s) should be executed (if any values match the test will be executed)
	 */
	Phase[] phase() default Phase.NOT_DEFINED;

	/**
	 * The array of tags for which the test(s) should be executed (if any
	 */
	String[] tags() default "";

	/**
	 * The deviceType(s) on which the test case(s) should be executed.
	 */
	DeviceType[] devicetype() default DeviceType.NOT_DEFINED;

	/**
	 * The platformType(s) on which the test case(s) should be executed.
	 */
	Platform[] platform() default Platform.NOT_DEFINED;

}
