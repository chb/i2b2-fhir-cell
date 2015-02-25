package org.bch.i2me2.core.security;

import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.security.auth.login.LoginException;
/**
 * The main class to perform API authentication
 * @author CH176656
 *
 */
//TODO: Unit tests!
public final class APIAuthenticator {
	private static APIAuthenticator apiAuthenticator = null;
	
	
	public static String MEDREC_APP_NAME = "MedRecApp";
	public static String MEDREC_APP_KEY = "cwjEk=dUsYDEgW9f-Cw-lF21K-qBeIkOu_UD92nWEr1hUiK34l34sn=Jceoa";
	
	// The map with Services Keys <service_key, app> 
	private final Map<String, String> serviceKeysStorage = new HashMap();
	
	// The map with valid security token informations: <token, service_key> 
	private final Map<String, String> authorizationTokensStorage = new HashMap();
	
	public APIAuthenticator() {
	}
	
	public static APIAuthenticator getInstance() {
		if (apiAuthenticator==null) {
			apiAuthenticator = new APIAuthenticator();
		}
		return apiAuthenticator;
	}
	
	/**
	 * Return the authentication token for the future API calls
	 * @param serviceKey	The service key associated with the authorized app
	 * @param app			The name of the remote app; 
	 * @return				The authorization token
	 * @throws LoginException	If app name does not match with the serviceKey parameter
	 */
	//TODO: We should include a timestamp to implement temporal tokens  
	public String getToken(String serviceKey, String app) throws LoginException {
		if (serviceKeysStorage.containsKey(serviceKey)) {
			String appName = serviceKeysStorage.get(serviceKey);
			if (appName.equals(app)) {
				String authToken = UUID.randomUUID().toString();
				authorizationTokensStorage.put(authToken,serviceKey);
				return authToken;
			}
		}
		throw new LoginException("Application not authorized. App: " + app);
	}
	
	/**
	 * Validates if a client that is calling the REST API is authorized  
	 * @param serviceKey
	 * @param authToken
	 * @return True if the token is valid for the current session and for the current
	 */
	//TODO: When timestamp is included in generated token, validate it
	public boolean isTokenValid(String serviceKey, String authToken) {
		if (authorizationTokensStorage.containsKey(authToken)) {
			String storedServiceKey = authorizationTokensStorage.get(authToken);
			return storedServiceKey.equals(serviceKey);
		}
		return false;
	}
	
	/**
	 * Releases the authorization token, emulating a logout action 
	 * @param serviceKey
	 * @param authToken
	 * @throws GeneralSecurityException	If token and service key does not match
	 */
	public void releaseToken(String serviceKey, String authToken) throws GeneralSecurityException{
		if (authorizationTokensStorage.containsKey(authToken)) {
			String storedServiceKey = authorizationTokensStorage.get(authToken);
			if (storedServiceKey.equals(serviceKey)) {
				authorizationTokensStorage.remove(authToken);
				return;
			}
		}
		throw new GeneralSecurityException("Invalid key and token");
	}
	
	// For testing purposes only
	public void addServiceKey(String serviceKey, String appName) {
		//TODO: just for Testing purposes
		this.serviceKeysStorage.put(serviceKey, appName);		
	}
}
