package org.bch.i2me2.core.security;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.security.GeneralSecurityException;

import javax.security.auth.login.LoginException;

import org.junit.Test;

/**
 * Basic unit tests for APIAuthentication class
 * @author CH176656
 *
 */
public class APIAuthenticationTest {

	@Test
    public void getTokenTestBaseCase() throws Exception {
		APIAuthenticator apiAuth = APIAuthenticator.getInstance();
		
		// Test1: null parameters
		try {
			apiAuth.getToken(null, null);
			fail("Exception was expected");
		} catch (LoginException e) {
			// Its totally fine
		}
		// Test2: only one parameter
		try {
			apiAuth.getToken("hola", null);
			fail("Exception was expected");
		} catch (LoginException e) {
			// Its totally fine
		}
		
		// Test3: only one parameter
		try {
			apiAuth.getToken(null, "hola");
			fail("Exception was expected");
		} catch (LoginException e) {
			// Its totally fine
		}
		
		// Test4: service key nor app exists
		try {
			apiAuth.getToken("not real", "not real");
			fail("Exception was expected");
		} catch (LoginException e) {
			// Its totally fine
		}
		
	}
	
	@Test
	public void getTokenTestGeneralCase() throws Exception {
		APIAuthenticator apiAuth = APIAuthenticator.getInstance();
		// Test1: App exists, but not service key
		try {
			apiAuth.getToken("not real", APIAuthenticator.MEDREC_APP_NAME);
			fail("Exception was expected");
		} catch (LoginException e) {
			// Its totally fine
		}
		
		// Test2: Service token exists, but not app
		try {
			apiAuth.getToken(APIAuthenticator.MEDREC_APP_KEY, "not real");
			fail("Exception was expected");
		} catch (LoginException e) {
			// Its totally fine
		}
		
		// Test3: Happy path
		String token = apiAuth.getToken(APIAuthenticator.MEDREC_APP_KEY, APIAuthenticator.MEDREC_APP_NAME);
		assertEquals(36, token.length());
	}
	
	@Test
	public void isTokenValidTestBaseCase() throws Exception {
		APIAuthenticator apiAuth = APIAuthenticator.getInstance();
		boolean b;
		// Test1: null values
		b = apiAuth.isTokenValid(null, null);
		assertFalse(b);
		
		// Test 2: only one value is null
		b = apiAuth.isTokenValid(null, "not null");
		assertFalse(b);
		b = apiAuth.isTokenValid("not null", null);
		assertFalse(b);
		
		// Test 3: both not nulls but not valid
		b = apiAuth.isTokenValid("not null", "not null");
		assertFalse(b);
	}
	
	@Test
	public void isTokenValidTestGeneralCase() throws Exception {
		APIAuthenticator apiAuth = APIAuthenticator.getInstance();
		String token = apiAuth.getToken(APIAuthenticator.MEDREC_APP_KEY, APIAuthenticator.MEDREC_APP_NAME);
		
		boolean b;
		
		// Test1: We have a valid token but we do not pass it right
		b = apiAuth.isTokenValid(APIAuthenticator.MEDREC_APP_KEY, "not valid token");
		assertFalse(b);
		
		// test2: We pass the valid token but not the service key
		b = apiAuth.isTokenValid("not valid key", token);
		assertFalse(b);
		
		// test3: Happy path
		b = apiAuth.isTokenValid(APIAuthenticator.MEDREC_APP_KEY, token);
		assertTrue(b);
	}
	
	@Test
	public void releaseTokenTestBaseCase() throws Exception {
		APIAuthenticator apiAuth = APIAuthenticator.getInstance();
		
		// Test1: nulls
		try {
			apiAuth.releaseToken(null, null);
			fail("Exception was expected");
		} catch (GeneralSecurityException e) {
			// Its totally fine
		}
		
		// Test2: only one null
		try {
			apiAuth.releaseToken("not null", null);
			fail("Exception was expected");
		} catch (GeneralSecurityException e) {
			// Its totally fine
		}
		
		try {
			apiAuth.releaseToken(null,"not null");
			fail("Exception was expected");
		} catch (GeneralSecurityException e) {
			// Its totally fine
		}
		
		// Test3: no nulls but bad
		try {
			apiAuth.releaseToken("not null","not null");
			fail("Exception was expected");
		} catch (GeneralSecurityException e) {
			// Its totally fine
		}
	}
	
	@Test
	public void releaseTokenTestGeneralCase() throws Exception {
		APIAuthenticator apiAuth = APIAuthenticator.getInstance();
		String token = apiAuth.getToken(APIAuthenticator.MEDREC_APP_KEY, APIAuthenticator.MEDREC_APP_NAME);
		assertTrue(apiAuth.isTokenValid(APIAuthenticator.MEDREC_APP_KEY, token));
		
		// Test1: we pass a valid key, but invalid token
		try {
			apiAuth.releaseToken(APIAuthenticator.MEDREC_APP_KEY,"not valid");
			fail("Exception was expected");
		} catch (GeneralSecurityException e) {
			// Its totally fine
			// We make sure token is still valid
			assertTrue(apiAuth.isTokenValid(APIAuthenticator.MEDREC_APP_KEY, token));
		}
		
		// Test2: we pass the valid token but an invalid key
		try {
			apiAuth.releaseToken("not valid",token);
			fail("Exception was expected");
		} catch (GeneralSecurityException e) {
			// We make sure token is still valid
			assertTrue(apiAuth.isTokenValid(APIAuthenticator.MEDREC_APP_KEY, token));
		}
		
		// Test3: Happy path
		apiAuth.releaseToken(APIAuthenticator.MEDREC_APP_KEY, token);
		// And we make sure that the token is no longer valid
		assertFalse(apiAuth.isTokenValid(APIAuthenticator.MEDREC_APP_KEY, token));
	}
	
	@Test
	public void releaseTokenTestSpecificCase() throws Exception {
		// Test to validate that the same App can request two different tokens without any problem and are 
		// properly released
		APIAuthenticator apiAuth = APIAuthenticator.getInstance();
		String token1 = apiAuth.getToken(APIAuthenticator.MEDREC_APP_KEY, APIAuthenticator.MEDREC_APP_NAME);
		String token2 = apiAuth.getToken(APIAuthenticator.MEDREC_APP_KEY, APIAuthenticator.MEDREC_APP_NAME);
		String token3 = apiAuth.getToken(APIAuthenticator.MEDREC_APP_KEY, APIAuthenticator.MEDREC_APP_NAME);
		
		// All tokens are valid
		assertTrue(apiAuth.isTokenValid(APIAuthenticator.MEDREC_APP_KEY, token1));
		assertTrue(apiAuth.isTokenValid(APIAuthenticator.MEDREC_APP_KEY, token2));
		assertTrue(apiAuth.isTokenValid(APIAuthenticator.MEDREC_APP_KEY, token3));
		
		// We release token 2,  
		apiAuth.releaseToken(APIAuthenticator.MEDREC_APP_KEY, token2);
		
		// And we check that everything is consistent
		assertTrue(apiAuth.isTokenValid(APIAuthenticator.MEDREC_APP_KEY, token1));
		assertFalse(apiAuth.isTokenValid(APIAuthenticator.MEDREC_APP_KEY, token2));
		assertTrue(apiAuth.isTokenValid(APIAuthenticator.MEDREC_APP_KEY, token3));
		
		// Now we release token 1 and 3
		apiAuth.releaseToken(APIAuthenticator.MEDREC_APP_KEY, token1);
		apiAuth.releaseToken(APIAuthenticator.MEDREC_APP_KEY, token3);
		
		// And we check that everything is consistent
		assertFalse(apiAuth.isTokenValid(APIAuthenticator.MEDREC_APP_KEY, token1));
		assertFalse(apiAuth.isTokenValid(APIAuthenticator.MEDREC_APP_KEY, token2));
		assertFalse(apiAuth.isTokenValid(APIAuthenticator.MEDREC_APP_KEY, token3));
		
	}
}
