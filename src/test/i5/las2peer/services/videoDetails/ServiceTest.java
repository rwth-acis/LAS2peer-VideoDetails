package i5.las2peer.services.videoDetails;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import i5.las2peer.p2p.LocalNode;
import i5.las2peer.security.ServiceAgent;
import i5.las2peer.security.UserAgent;
import i5.las2peer.testing.MockAgentFactory;
import i5.las2peer.webConnector.WebConnector;
import i5.las2peer.webConnector.client.ClientResponse;
import i5.las2peer.webConnector.client.MiniClient;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 * Example Test Class demonstrating a basic JUnit test structure.
 * 
 * 
 *
 */
public class ServiceTest {
	
	private static final String HTTP_ADDRESS = "http://127.0.0.1";
	private static final int HTTP_PORT = WebConnector.DEFAULT_HTTP_PORT;
	
	private static LocalNode node;
	private static WebConnector connector;
	private static ByteArrayOutputStream logStream;
	
	private static UserAgent testAgent;
	private static final String testPass = "adamspass";
	
	private static final String testServiceClass = "i5.las2peer.services.videoDetails.VideoDetailsClass";
	
	private static final String mainPath = "video-details/";
	
	
	/**
	 * Called before the tests start.
	 * 
	 * Sets up the node and initializes connector and users that can be used throughout the tests.
	 * 
	 * @throws Exception
	 */
	@BeforeClass
	public static void startServer() throws Exception {
		
		//start node
		node = LocalNode.newNode();
		node.storeAgent(MockAgentFactory.getAdam());
		node.launch();
		
		ServiceAgent testService = ServiceAgent.generateNewAgent(testServiceClass, "a pass");
		testService.unlockPrivateKey("a pass");
		
		node.registerReceiver(testService);
		
		//start connector
		logStream = new ByteArrayOutputStream ();
		
		connector = new WebConnector(true,HTTP_PORT,false,1000);
		connector.setSocketTimeout(10000);
		connector.setLogStream(new PrintStream (logStream));
		connector.start ( node );
        Thread.sleep(1000); //wait a second for the connector to become ready
		testAgent = MockAgentFactory.getAdam();
		
        connector.updateServiceList();
        //avoid timing errors: wait for the repository manager to get all services before continuing
        try
        {
            System.out.println("waiting..");
            Thread.sleep(10000);
        }
        catch(InterruptedException e)
        {
            e.printStackTrace();
        }
		
	}
	
	
	/**
	 * Called after the tests have finished.
	 * Shuts down the server and prints out the connector log file for reference.
	 * 
	 * @throws Exception
	 */
	@AfterClass
	public static void shutDownServer () throws Exception {
		
		connector.stop();
		node.shutDown();
		
        connector = null;
        node = null;
        
        LocalNode.reset();
		
		System.out.println("Connector-Log:");
		System.out.println("--------------");
		
		System.out.println(logStream.toString());
		
    }
	
	
	/**
	 * 
	 * Tests the validation method.
	 * 
	 */
	@Test
	public void testValidateLogin()
	{
		MiniClient c = new MiniClient();
		c.setAddressPort(HTTP_ADDRESS, HTTP_PORT);
		
		try
		{
			c.setLogin(Long.toString(testAgent.getId()), testPass);
            ClientResponse result=c.sendRequest("GET", mainPath +"validation", "");
            assertEquals(200, result.getHttpCode());
            assertTrue(result.getResponse().trim().contains("adam")); //login name is part of response
			System.out.println("Result of 'testValidateLogin': " + result.getResponse());
		}
		catch(Exception e)
		{
			e.printStackTrace();
			fail ( "Exception: " + e );
		}
		
    }
	
	/**
	 * Test method to insert details of one particular video
	 */
	
	@Test
	public void testInsertVideo()
	{
		MiniClient c = new MiniClient();
		c.setAddressPort(HTTP_ADDRESS, HTTP_PORT);
		
		try
		{
			c.setLogin(Long.toString(testAgent.getId()), testPass);
            ClientResponse result=c.sendRequest("POST", mainPath +"videos", "{ \"videoid\":\"10\", "
            		+ "\"url\":\"url10\",  \"thumbnail\":\"thumbnail10\", "
            		+ "\"uploader\":\"uploader10\", \"tool\":\"tool10\", "
            		+ "\"community\":\"community10\", \"description\":\"description10\", "
            		+ " \"language\":\"en\" }"); 
            assertEquals(200, result.getHttpCode());
            assertTrue(result.getResponse().trim().contains("inserted")); 
			System.out.println("Result of 'testInsertVideo': " + result.getResponse().trim());
			ClientResponse delete=c.sendRequest("DELETE", mainPath +"videos/10", ""); 
            assertEquals(200, delete.getHttpCode());
            assertTrue(delete.getResponse().trim().contains("deleted"));
		}
		catch(Exception e)
		{
			e.printStackTrace();
			fail ( "Exception: " + e );
		}
		
    }
	
	/**
	 * Test method to update details of one particular video
	 */
	
	@Test
	public void testUpdateVideo()
	{
		MiniClient c = new MiniClient();
		c.setAddressPort(HTTP_ADDRESS, HTTP_PORT);
		
		try
		{
			c.setLogin(Long.toString(testAgent.getId()), testPass);
            ClientResponse result=c.sendRequest("POST", mainPath +"videos/4", "{\"url\":\"urlFromTest\"}"); 
            assertEquals(200, result.getHttpCode());
            assertTrue(result.getResponse().trim().contains("updated")); 
			System.out.println("Result of 'testUpdateVideo': " + result.getResponse().trim());
		}
		catch(Exception e)
		{
			e.printStackTrace();
			fail ( "Exception: " + e );
		}
		
    }
	
	/**
	 * Test method to delete  one particular video
	 */
	
	@Test
	public void testDeleteVideo()
	{
		MiniClient c = new MiniClient();
		c.setAddressPort(HTTP_ADDRESS, HTTP_PORT);
		
		try
		{
			c.setLogin(Long.toString(testAgent.getId()), testPass);
			ClientResponse insert=c.sendRequest("POST", mainPath +"videos", "{ "
					+ "\"videoid\":\"11\", \"url\":\"url11\",  \"thumbnail\":\"thumbnail11\","
					+ " \"uploader\":\"uploader11\", \"tool\":\"tool11\", "
					+ "\"community\":\"community11\", \"description\":\"description11\", "
            		+ " \"language\":\"en\" }");
			assertEquals(200, insert.getHttpCode());
            assertTrue(insert.getResponse().trim().contains("inserted")); 
			ClientResponse result=c.sendRequest("DELETE", mainPath +"videos/11", ""); 
            assertEquals(200, result.getHttpCode());
            assertTrue(result.getResponse().trim().contains("deleted")); 
			System.out.println("Result of 'testDeleteVideo': " + result.getResponse().trim());
		}
		catch(Exception e)
		{
			e.printStackTrace();
			fail ( "Exception: " + e );
		}
		
    }
	
	
	
	/**
	 * Test method to get details many videos
	 */
	@Test
	public void testGetVideos(){
		MiniClient c = new MiniClient();
		c.setAddressPort(HTTP_ADDRESS, HTTP_PORT);
		
		try
		{
			c.setLogin(Long.toString(testAgent.getId()), testPass);
            ClientResponse result=c.sendRequest("GET", mainPath +"videos", ""); //testInput is the pathParam
            assertEquals(200, result.getHttpCode());
            assertTrue(result.getResponse().contains("1")); //"testInput" name is part of response
			System.out.println("Result of 'testGetVideos': " + result.getResponse().trim());
		}
		catch(Exception e)
		{
			e.printStackTrace();
			fail ( "Exception: " + e );
		}
	}
	
	/**
	 * Test method to get details of one particular video
	 */
	@Test
	public void testGetVideo(){
		MiniClient c = new MiniClient();
		c.setAddressPort(HTTP_ADDRESS, HTTP_PORT);
		
		try
		{
			c.setLogin(Long.toString(testAgent.getId()), testPass);
            ClientResponse result=c.sendRequest("GET", mainPath +"videos/1", ""); //testInput is the pathParam
            assertEquals(200, result.getHttpCode());
            assertTrue(result.getResponse().contains("1")); //"testInput" name is part of response
			System.out.println("Result of 'testGetVideo': " + result.getResponse().trim());
		}
		catch(Exception e)
		{
			e.printStackTrace();
			fail ( "Exception: " + e );
		}
	}
	
	

	/**
	 * Test the ServiceClass for valid rest mapping.
	 * Important for development.
	 */
	@Test
	public void testDebugMapping()
	{
		VideoDetailsClass cl = new VideoDetailsClass();
		assertTrue(cl.debugMapping());
	}
}
