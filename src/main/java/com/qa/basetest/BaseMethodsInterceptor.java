package com.qa.basetest;

import com.qa.basetest.tags.*;
import com.qa.utils.Reporter;
import org.testng.IMethodInstance;
import org.testng.IMethodInterceptor;
import org.testng.ITestContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BaseMethodsInterceptor implements IMethodInterceptor {

	private List<String> testPhases = null;
	private List<String> tags = null;

	public BaseMethodsInterceptor() {
		testPhases = retrieveSystemProperty("testPhase");
		tags = retrieveSystemProperty("tags");
	}

	private List<String> retrieveSystemProperty(String systemPropertyName) {
		String systemPropertyValue = System.getProperty(systemPropertyName);
		if (systemPropertyValue != null && !systemPropertyValue.isEmpty()) {
			return returnStringAsList(systemPropertyValue.toLowerCase());
		}
		return null;
	}

	private List<String> returnStringAsList(String stringValue) {
		stringValue.toLowerCase();
		List<String> array = Arrays.asList(stringValue.split("\\,"));
		for (int i = 0; i < array.size(); i++) {
			array.set(i, array.get(i).trim());
		}
		return array;
	}

	/**
	 * The interceptor goes through each test and it's annotations and checks our custom rules to see if the test should be run
	 *
	 */
	public List<IMethodInstance> intercept(List<IMethodInstance> methods, ITestContext context) {
		// list to add test cases to be executed.
		List<IMethodInstance> result = new ArrayList<IMethodInstance>();

		// go through each of the test methods and check if they meet the criteria set out.
		for (IMethodInstance m : methods) {

        	//grab the platform name and device type from the test
			String deviceType = null;
			String platformName = null;
			if (m.getInstance() instanceof MultiDeviceBaseTest) {
				MultiDeviceBaseTest testInstance = (MultiDeviceBaseTest) m.getInstance();
				deviceType = testInstance.getDeviceType();
				platformName = testInstance.getPlatformName();
			}

			// grab the TestTags annotations
			TestTags methodTestTags = m.getMethod().getConstructorOrMethod().getMethod().getAnnotation(TestTags.class);
			TestTags classTestTags = (TestTags) m.getMethod().getTestClass().getRealClass().getAnnotation(TestTags.class);

			// check all the different conditions to see if the test matches them.
			Boolean phasesMatch = doesTestMatchTestPhasesCriteria(classTestTags, methodTestTags);
			Boolean tagsMatch = doesTestMatchTestTagsCriteria(classTestTags, methodTestTags);
			Boolean deviceTypeMatch = doesTestMatchDeviceTypesCriteria(classTestTags, methodTestTags, deviceType);
			Boolean platformNameMatch = doesTestMatchPlatformNameCriteria(classTestTags, methodTestTags, platformName);

			Reporter.info("Test [" + m.getMethod().getMethodName().toString() + "] matching group values, phases: [" + phasesMatch + "], tags: [" + tagsMatch
					+ "], deviceType: [" + deviceTypeMatch + "], plaformName: [" + platformNameMatch + "]");

			if (phasesMatch && tagsMatch && deviceTypeMatch && platformNameMatch) {
				//if the test matches the conditions then add it to the list to be executed
				result.add(m);
			}
		}
		return result;
	}

	private Boolean doesTestMatchTestPhasesCriteria(TestTags classTestTags, TestTags methodTestTags) {
		// if no phases past as argument then don't apply a filter
		if (this.testPhases == null) {
			return true;
		}
		// check if test phases were configured at class level
		Phase[] classLevelPhases = null;
		if (classTestTags != null) {
			classLevelPhases = classTestTags.phase();
		}
		// check if test phases were configured at method level
		Phase[] methodLevelPhases = null;
		if (methodTestTags != null) {
			methodLevelPhases = methodTestTags.phase();
		}
		// now go through each test phase in the arguments and see if either class or method info match it.
		for (String testPhase : testPhases) {
			if (doesEnumArrayContainValue(Phase.class, classLevelPhases, testPhase) || doesEnumArrayContainValue(Phase.class, methodLevelPhases, testPhase)) {
				return true;
			}
		}
		return false;
	}

	private Boolean doesTestMatchTestTagsCriteria(TestTags classTestTags, TestTags methodTestTags) {

		String[] classLevelTags = null;
		if (classTestTags != null) {
			classLevelTags = classTestTags.tags();
		}

		String[] methodLevelTags = null;
		if (methodTestTags != null) {
			methodLevelTags = methodTestTags.tags();
		}
		return doesTestMatchAnyCriteria(classLevelTags, methodLevelTags, this.tags);
	}

	private Boolean doesTestMatchDeviceTypesCriteria(TestTags classTestTags, TestTags methodTestTags, String deviceTypeCurrentDevice) {
		if (deviceTypeCurrentDevice == null) {
			return true;
		}

		// check if deviceType was configured at class level
		DeviceType[] classLevelDeviceTypes = null;
		if (classTestTags != null) {
			classLevelDeviceTypes = classTestTags.devicetype();
		}
		// check if test deviceType was configured at method level
		DeviceType[] methodLevelDeviceTypes = null;
		if (methodTestTags != null) {
			methodLevelDeviceTypes = methodTestTags.devicetype();
		}

		//check if any deviceType tags were added at class/method level
		if ((classLevelDeviceTypes == null || (Arrays.asList(classLevelDeviceTypes).size() == 1 && Arrays.asList(classLevelDeviceTypes).get(0).equals(DeviceType.NOT_DEFINED)))
				&& (methodLevelDeviceTypes == null || (Arrays.asList(methodLevelDeviceTypes).size() == 1 && Arrays.asList(methodLevelDeviceTypes).get(0).equals(DeviceType.NOT_DEFINED)))) {
			return true;
		}

		//check deviceType passed is valid
		DeviceType currentDeviceType = DeviceType.valueOf(deviceTypeCurrentDevice.toUpperCase());
		if (currentDeviceType == null) {
			Reporter.error("The device type provide is invalid [" + deviceTypeCurrentDevice + "]");
		}

		// now go through each device type in the arguments and see if either class or method info match it.
		if (doesEnumArrayContainValue(DeviceType.class, classLevelDeviceTypes, deviceTypeCurrentDevice) || doesEnumArrayContainValue(DeviceType.class, methodLevelDeviceTypes, deviceTypeCurrentDevice)) {
			return true;
		}
		return false;
	}

	private Boolean doesTestMatchPlatformNameCriteria(TestTags classTestTags, TestTags methodTestTags, String platformNameCurrentDevice) {
		if (platformNameCurrentDevice == null) {
			return true;
		}

		// check if deviceType was configured at class level
		Platform[] classLevelPlatformNames = null;
		if (classTestTags != null) {
			classLevelPlatformNames = classTestTags.platform();
		}
		// check if test deviceType was configured at method level
		Platform[] methodLevelPlatformNames = null;
		if (methodTestTags != null) {
			methodLevelPlatformNames = methodTestTags.platform();
		}

		//check if any deviceType tags were added at class/method level
		if ((classLevelPlatformNames == null || (Arrays.asList(classLevelPlatformNames).size() == 1 && Arrays.asList(classLevelPlatformNames).get(0).equals(Platform.NOT_DEFINED)))
				&& (methodLevelPlatformNames == null || (Arrays.asList(methodLevelPlatformNames).size() == 1 && Arrays.asList(methodLevelPlatformNames).get(0).equals(Platform.NOT_DEFINED)))) {
			return true;
		}

		//check deviceType passed is valid
		Platform currentPlatformNames = Platform.valueOf(platformNameCurrentDevice.toUpperCase());
		if (currentPlatformNames == null) {
			Reporter.error("The device type provide is invalid [" + platformNameCurrentDevice + "]");
		}

		// now go through each device type in the arguments and see if either class or method info match it.
		if (doesEnumArrayContainValue(Platform.class, classLevelPlatformNames, platformNameCurrentDevice) || doesEnumArrayContainValue(Platform.class, methodLevelPlatformNames, platformNameCurrentDevice)) {
			return true;
		}
		return false;
	}

	private Boolean doesTestMatchAnyCriteria(String[] classTagsString, String[] methodTagsString, List<String> criteriaList) {
		if (criteriaList == null) {
			// this means we havn't ask for it to filter on this property.
			return true;
		}
		if (methodTagsString != null) {
			// first take the method string
			for (String methodValue : methodTagsString) {
				if (criteriaList.contains(methodValue)) {
					return true;
				}
			}
		}
		if (classTagsString != null) {
			// as a final we take the parent class string.
			for (String classValue : classTagsString) {
				if (criteriaList.contains(classValue)) {
					return true;
				}
			}
		}
		return false;
	}

	public static <E extends Enum<E>> boolean doesEnumArrayContainValue(Class<E> clazz, E[] enumArray, String stringToLookFor) {
		if (enumArray != null) {
			for (E en : enumArray) {
				if (en.name().equalsIgnoreCase(stringToLookFor)) {
					return true;
				}
			}
		}
		return false;
	}
}