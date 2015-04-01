/**
 * Copyright 2014 IBM Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/
package com.ibm.amc.data.validation;

import java.lang.reflect.Field;
import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ibm.amc.Constants;
import com.ibm.amc.data.validation.annotations.ValidCharacters;
import com.ibm.amc.data.validation.annotations.ValidLength;
import com.ibm.amc.data.validation.annotations.ValidNumber;
import com.ibm.amc.data.validation.annotations.ValidRegex;
import com.ibm.amc.data.validation.annotations.ValidUri;
import com.ibm.amc.data.validation.annotations.ValidUrl;
import com.ibm.amc.data.validation.annotations.ValidateNotBlank;
import com.ibm.amc.data.validation.annotations.ValidateNotNull;
import com.ibm.amc.data.validation.annotations.ValidatedAs;
import com.ibm.amc.ras.Logger47;
import com.ibm.amc.resources.exceptions.AmcIllegalArgumentException;

/**
 * An engine which reads the validation annotations on data classes and checks that the data
 * conforms to the restrictions expressed by those annotations.
 */
public class ValidationEngine
{
	// @CLASS-COPYRIGHT@

	static Logger47 logger = Logger47.get(ValidationEngine.class.getCanonicalName(), Constants.CWZBA_BUNDLE_NAME);

	private static ValidationEngine instance;

	/**
	 * Get a ValidationEngine to use.
	 */
	public static ValidationEngine getInstance()
	{
		if (instance == null) instance = new ValidationEngine();
		return instance;
	}

	/**
	 * Validate the fields in an AbstractData according to the validation rules with which the
	 * fields are annotated.
	 * 
	 * @param resource
	 *            The data object to validate
	 * @throws AmcIllegalArgumentException
	 *             if any field in the object contains data which is not valid according to its
	 *             self-declared rules or the resource itself declares that it is not valid.
	 */
	public void validate(Object resource) throws AmcIllegalArgumentException
	{
		Field[] fields = resource.getClass().getDeclaredFields();

		nextField: for (Field field : fields)
		{
			if (field.isAnnotationPresent(JsonIgnore.class)) continue nextField;
			if (field.getName().equals("this$0")) continue nextField; // Ignore parent instance
																		// reference when resource
																		// is an anonymous inner
																		// class - only relevant for
																		// unit tests.
			validate(field, resource);
		}

		if (resource instanceof SelfValidating)
		{
			((SelfValidating) resource).validate();
		}

	}

	private void validate(Field field, Object resource) throws InvalidDataException
	{
		boolean validated = false;

		// Call the getter to get the value to validate.
		Object value = invoke(field, resource);

		if (field.isAnnotationPresent(ValidateNotBlank.class))
		{
			boolean valid = new ValidateNotBlank.NotBlankValidator().validate(value, null);

			// Note we don't set validated as true for this - non-blank isn't enough to ensure
			// safety.

			if (!valid) throw new InvalidDataException(field.getName());
		}

		if (field.isAnnotationPresent(ValidateNotNull.class))
		{
			boolean valid = new ValidateNotNull.NotNullValidator().validate(value, null);

			// Note we don't set validated as true for this - non-null isn't enough to ensure
			// safety.

			if (!valid) throw new InvalidDataException(field.getName());
		}

		/* only perform further validation if the value isn't null */
		if (value != null)
		{
			if (field.isAnnotationPresent(ValidCharacters.class))
			{
				ValidCharacters constraints = field.getAnnotation(ValidCharacters.class);
				boolean valid = new ValidCharacters.ValidCharacterValidator().validate(value, constraints);
				validated = true;

				if (!valid) throw new InvalidDataException(field.getName());
			}

			if (field.isAnnotationPresent(ValidNumber.class))
			{
				ValidNumber constraints = field.getAnnotation(ValidNumber.class);
				boolean valid = new ValidNumber.NumberValidator().validate(value, constraints);
				validated = true;

				if (!valid) throw new InvalidDataException(field.getName());
			}

			if (field.isAnnotationPresent(ValidLength.class))
			{
				ValidLength constraints = field.getAnnotation(ValidLength.class);
				boolean valid = new ValidLength.ValidLengthValidator().validate(value, constraints);
				validated = true;

				if (!valid) throw new InvalidDataException(field.getName());
			}

			if (field.isAnnotationPresent(ValidRegex.class))
			{
				ValidRegex constraints = field.getAnnotation(ValidRegex.class);
				boolean valid = new ValidRegex.RegexValidator().validate(value, constraints);
				validated = true;

				if (!valid) throw new InvalidDataException(field.getName());
			}

			if (field.isAnnotationPresent(ValidUri.class))
			{
				ValidUri constraints = field.getAnnotation(ValidUri.class);
				boolean valid = new ValidUri.ValidUriValidator().validate(value, constraints);
				validated = true;

				if (!valid) throw new InvalidDataException(field.getName());
			}

			if (field.isAnnotationPresent(ValidUrl.class))
			{
				ValidUrl constraints = field.getAnnotation(ValidUrl.class);
				boolean valid = new ValidUrl.ValidUrlValidator().validate(value, constraints);
				validated = true;

				if (!valid) throw new InvalidDataException(field.getName());
			}

			if (field.isAnnotationPresent(ValidatedAs.class))
			{
				boolean valid = false;
				Class<? extends Validator> validatorClass = field.getAnnotation(ValidatedAs.class).value();
				try
				{
					valid = validatorClass.newInstance().validate(value, null);
					validated = true;
				}
				catch (IllegalAccessException e)
				{
					// Should never happen, unless we start using complex access control stuff in
					// which case proper handling should be added.
					logger.debug("invoke", "IllegalAccessException instantiating a validator", e);
					// Fall through and treat the data as invalid.
				}
				catch (InstantiationException e)
				{
					// Should never happen, because we only instantiate Validators, which aren't
					// susceptible to this.
					logger.debug("invoke", "InstantiationException instantiating a validator", e);
					// Fall through and treat the data as invalid.
				}

				if (!valid) throw new InvalidDataException(field.getName());
			}

			if (!validated)
			{
				doGenericValidation(value, field.getName());
			}
		}
	}

	private Object invoke(Field field, Object resource)
	{
		try
		{
			return field.get(resource);
		}
		catch (IllegalArgumentException e)
		{
			// Should never happen, because we only call no-arg getters on the object we found them
			// in.
			logger.debug("invoke", "IllegalArgumentException invoking an accessor method on an AbstractData subclass", e);
			throw new RuntimeException(e);
		}
		catch (IllegalAccessException e)
		{
			// Should never happen, unless we start using complex access control stuff in which case
			// proper handling should be added.
			logger.debug("invoke", "IllegalAccessException invoking an accessor method on an AbstractData subclass", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Filter potential malicious content out of a value for which no specific validation rules were
	 * supplied.
	 * 
	 * @throws InvalidDataException
	 *             if malicious content is found.
	 */
	private void doGenericValidation(Object value, String fieldName) throws InvalidDataException
	{
		if (Number.class.isAssignableFrom(value.getClass())) return; // Numbers can't hurt us.
		if (Boolean.class.isAssignableFrom(value.getClass())) return; // Booleans can't hurt us.
		if (value.getClass().isArray())
		{
			// Validate array contents
			for (Object object: (Object[]) value)
			{
				doGenericValidation(object, fieldName);
			}
		}
		else if (value instanceof Collection)
		{
			@SuppressWarnings("rawtypes")
			final Collection collection = (Collection) value;
			for (Object object: collection)
			{
				doGenericValidation(object, fieldName);
			}
		}
		else if (value instanceof String)
		{
			// This is a placeholder, to be replaced with AntiSamy validation in a future story.
			// TODO - Proper anti-XSS validation not implemented yet
			if (((String) value).contains("<script")) throw new InvalidDataException(fieldName);
		}
		else
		{
			logger.debug("doGenericValidation", "Generic validation of non-String, non-Number, non-Boolean types not implemented:" + value.getClass());
			throw new InvalidDataException(fieldName);
		}
	}

}
