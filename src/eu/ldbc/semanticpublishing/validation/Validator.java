package eu.ldbc.semanticpublishing.validation;

import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;

public class Validator {
	
	/**
	 * @param result - result from query
	 * @param parameterValues - array of parameter values expected in the result
	 * @param strict - forces full string comparison, if not strict - data types are not checked for
	 * @return 0 it all parameter values have been found in the result
	 * @throws UnsupportedEncodingException 
	 */
	protected int validateEditorial(String result, String validateOperation, boolean resultIsFromAskQuery, int iteration, String[] parameterValues, boolean strict) throws UnsupportedEncodingException {
		int totalErrors = 0;
	
		if (resultIsFromAskQuery) {
			if (!result.toLowerCase().contains(">false<") && !result.toLowerCase().contains(">no<")) {			
				totalErrors++;
			}
		} else {		
			if (parameterValues == null) {		
				return 1;
			}
			
			for (int i = 0; i < parameterValues.length; i++) {
				int validationErrorsCount = 0;
				
				String value = parameterValues[i];
				
				//skip validation of context
				if (value.contains("/context/")) {
					continue;
				}
				
				value = transformString(value, strict, false);
				
				if (!result.contains(value)) {
					validationErrorsCount++;
				}
				
				//give it one more try, in case there are encodings in results which are not URLDecoded
				if (validationErrorsCount > 0) {
					value = transformString(value, strict, true);
					
					if (result.contains(value)) {
						validationErrorsCount--;						
					}
				}				
				
				if (validationErrorsCount > 0) {
					System.out.println(validateOperation + " validation failed on iteration : " + iteration + ", query result is missing value : " + value);
					totalErrors++;
				}
			}
		}
	
		return totalErrors;
	}
	
	protected int validateAggregate(String result, long actualResultsSize, long expectedResultSize, String validateOperation, int iteration, List<String> validationList, boolean strict) throws UnsupportedEncodingException {
		int totalErrors = 0;
		String value;
		String escapedString = StringEscapeUtils.escapeJava(result).replace("\\\\", "\\");
				
		for (String v : validationList) {
			int validationErrorsCount = 0;

			value = transformString(v, strict, false);
			if (!escapedString.contains(value)) {
				validationErrorsCount++;
			}
			
			//give it one more try, in case there are encodings in results which are not URLDecoded
			if (validationErrorsCount > 0) {
				value = transformString(v, strict, true);

				if (escapedString.contains(value)) {
					validationErrorsCount--;
				}
			}
			
			if (validationErrorsCount > 0) {
				System.out.println("\t\t" + /*validateOperation +*/ "validation failed on query : " + iteration + ", query result is missing value : " + value);
				totalErrors++;
			}
		}
		
		if (actualResultsSize < expectedResultSize) {
			System.out.println("\t\tWarning : " + /*validateOperation +*/ "validation failed on query : " + iteration + ", minimum expected amount of results : " + expectedResultSize + ",  actual : " + actualResultsSize);
			totalErrors++;
		}
		
		return totalErrors;
	}
	
	private String transformString(String str, boolean strict, boolean forceOptionalEncodings) {
		String result = str;
		if (!strict) {
			if (result.startsWith("<")) {
				result = result.substring(1);
			}
			if (result.endsWith(">")) {
				result = result.substring(0, result.length()-1);
			}				
			if (result.contains("^^")) {
				result = result.substring(0, result.indexOf("^^"));
			}
			
			result = result.replace("\"", "");
		}
		
		//escapeJava() returns UTF-8 escaped chars with double "\\"
		result = StringEscapeUtils.escapeJava(result).replace("\\\\", "\\");		
		
		result = customURLEncode(result, forceOptionalEncodings);
		
		return result;
	}
	
	private String customURLEncode(String str, boolean forceOptionalEncodings) {
		String result = str;
		if (str.contains("&amp;")) {
			result = str.replace("&amp;", "&");
		} else if (str.contains("&")) {
			result = str.replace("&", "&amp;");
		}
		if (forceOptionalEncodings) {
			if (str.contains("'")) {
				result = str.replace("'", "&#39;");
			}
		}
		return result;
	}
}
