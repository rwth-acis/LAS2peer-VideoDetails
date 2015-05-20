package i5.las2peer.services.videoDetails;

import i5.las2peer.api.Service;
import i5.las2peer.restMapper.HttpResponse;
import i5.las2peer.restMapper.MediaType;
import i5.las2peer.restMapper.RESTMapper;
import i5.las2peer.restMapper.annotations.ContentParam;
import i5.las2peer.restMapper.annotations.DELETE;
import i5.las2peer.restMapper.annotations.GET;
import i5.las2peer.restMapper.annotations.POST;
import i5.las2peer.restMapper.annotations.Path;
import i5.las2peer.restMapper.annotations.PathParam;
import i5.las2peer.restMapper.annotations.Produces;
import i5.las2peer.restMapper.annotations.QueryParam;
import i5.las2peer.restMapper.annotations.Version;
import i5.las2peer.restMapper.annotations.swagger.ApiInfo;
import i5.las2peer.restMapper.annotations.swagger.ApiResponses;
import i5.las2peer.restMapper.annotations.swagger.ApiResponse;
import i5.las2peer.restMapper.annotations.swagger.Notes;
import i5.las2peer.restMapper.annotations.swagger.ResourceListApi;
import i5.las2peer.restMapper.annotations.swagger.Summary;
import i5.las2peer.restMapper.tools.ValidationResult;
import i5.las2peer.restMapper.tools.XMLCheck;
import i5.las2peer.security.Context;
import i5.las2peer.security.UserAgent;
import i5.las2peer.services.videoDetails.database.DatabaseManager;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

import com.mysql.jdbc.ResultSetMetaData;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import net.minidev.json.parser.ParseException;

/**
 * LAS2peer Service
 * 
 * This is a LAS2peer service used to save details of videos uploaded in sevianno3
 * that uses the LAS2peer Web-Connector for RESTful access to it.
 * 
 */
@Path("video-details")
@Version("0.1")
@ApiInfo(
		title="Video details",
		description="<p>A RESTful service for saving details of the uploaded videos.</p>", 
				termsOfServiceUrl="",
				contact="bakiu@dbis.rwth-aachen.de",
				license="",
				licenseUrl=""
		)
public class VideoDetailsClass extends Service {
	
	private String jdbcDriverClassName;
	private String jdbcLogin;
	private String jdbcPass;
	private String jdbcUrl;
	private String jdbcSchema;
	private DatabaseManager dbm;
	
	private String epUrl;

	public VideoDetailsClass() {
		// read and set properties values
		setFieldValues();
		
		if(!epUrl.endsWith("/")){
			epUrl += "/";
		}
		// instantiate a database manager to handle database connection pooling and credentials
		dbm = new DatabaseManager(jdbcDriverClassName, jdbcLogin, jdbcPass, jdbcUrl, jdbcSchema);
	}
	

	/**
	 * Function to validate a user login.
	 * @return HttpRespons
	 * 
	 */
	@GET
	@Produces(MediaType.TEXT_HTML)
	@Path("validation")
	@ResourceListApi(description = "Check the user")
	@Summary("Return a greeting for the logged in user")
	@Notes("This is an example method")
	@ApiResponses(value={
			@ApiResponse(code = 200, message = "The user is logged in"),
	})
	public HttpResponse validateLogin() {
		String returnString = "";
		returnString += "You are " + ((UserAgent) getActiveAgent()).getLoginName() + " !";
		
		HttpResponse res = new HttpResponse(returnString);
		res.setStatus(200);
		return res;
	}
	
		/**
	 * Method that retrieves the video details from the database 
	 * and return an HTTP response including a JSON object.
	 * 
	 * @param videoid id of the video
	 * @param part projection for the query
	 * @return HttpResponse
	 * 
	 */
	@GET
	@Path("videos/{videoid}")
	@Produces("application/json")
	@ResourceListApi(description = "Return details for a selected video")
	@Summary("return a JSON with video details stored for the given VideoID")
	@Notes("query parameter selects the columns that need to be returned in the JSON.")
	@ApiResponses(value={
			@ApiResponse(code = 200, message = "Video details"),
			@ApiResponse(code = 404, message = "Video id does not exist"),
			@ApiResponse(code = 500, message = "Internal error"),
	})
	public HttpResponse getVideoDetails(@PathParam("videoid") String videoid, @QueryParam(name = "part", defaultValue = "*" ) String part) {
		String result = "";
		String columnName="";
		String selectquery ="";
		int columnCount = 0;
		Connection conn = null;
		PreparedStatement stmnt = null;
		ResultSet rs = null;
		ResultSetMetaData rsmd = null;
		try {
			// get connection from connection pool
			conn = dbm.getConnection();
			selectquery = "SELECT " + part + " FROM videodetails WHERE videoId = " + videoid + " ;" ;
			
			// prepare statement
			stmnt = conn.prepareStatement(selectquery);
			
			// retrieve result set
			rs = stmnt.executeQuery();
			rsmd = (ResultSetMetaData) rs.getMetaData();
			columnCount = rsmd.getColumnCount();
			
			// process result set
			if (rs.next()) {
				JSONObject ro = new JSONObject();
				for(int i=1;i<=columnCount;i++){
					result = rs.getString(i);
					columnName = rsmd.getColumnName(i);
					// setup resulting JSON Object
					ro.put(columnName, result);
					
				}
					
				
				// return HTTP Response on success
				HttpResponse r = new HttpResponse(ro.toJSONString());
				r.setStatus(200);
				return r;
				
			} else {
				result = "No result for Video " + videoid;
				
				// return HTTP Response on error
				HttpResponse er = new HttpResponse(result);
				er.setStatus(404);
				return er;
			}
		} catch (Exception e) {
			// return HTTP Response on error
			HttpResponse er = new HttpResponse("Internal error: " + e.getMessage());
			er.setStatus(500);
			return er;
		} finally {
			// free resources
			if (rs != null) {
				try {
					rs.close();
				} catch (Exception e) {
					Context.logError(this, e.getMessage());
					
					// return HTTP Response on error
					HttpResponse er = new HttpResponse("Internal error: " + e.getMessage());
					er.setStatus(500);
					return er;
				}
			}
			if (stmnt != null) {
				try {
					stmnt.close();
				} catch (Exception e) {
					Context.logError(this, e.getMessage());
					
					// return HTTP Response on error
					HttpResponse er = new HttpResponse("Internal error: " + e.getMessage());
					er.setStatus(500);
					return er;
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {
					Context.logError(this, e.getMessage());
					
					// return HTTP Response on error
					HttpResponse er = new HttpResponse("Internal error: " + e.getMessage());
					er.setStatus(500);
					return er;
				}
			}
		}
	}
	

	/**
	 * Method that retrieves the video details from the database 
	 * and return an HTTP response including a JSON object.
	 * 
	 * @param query the search of the user
	 * @param part projection for the query
	 * @return HttpResponse
	 * 
	 */
	@GET
	@Path("videos")
	@Produces("application/json")
	@ResourceListApi(description = "stored in a MySQL database")
	@Summary("return a JSON with details of videos stored")
	@Notes("query parametes q matches video description. query parameter part selects the columns that need to be returned in the JSON.")
	@ApiResponses(value={
			@ApiResponse(code = 200, message = "Details for the videos, JSON Array"),
			@ApiResponse(code = 404, message = "No videos exist"),
			@ApiResponse(code = 500, message = "Internal error"),
	})
	public HttpResponse getVideosDetail(@QueryParam(name="q",defaultValue="") String query, @QueryParam(name="part", defaultValue="*") String part) {
		String result = "";
		String columnName="";
		String selectquery ="";
		int columnCount = 0;
		Connection conn = null;
		PreparedStatement stmnt = null;
		ResultSet rs = null;
		ResultSetMetaData rsmd = null;
		JSONObject ro=null;
		JSONArray qs = new JSONArray();
		try {
			// get connection from connection pool
			conn = dbm.getConnection();
			selectquery = "SELECT " + part + " FROM videodetails WHERE description like '%" + query + "%' "
					+ " or community like '%" + query + "%' ;";
			// prepare statement
			stmnt = conn.prepareStatement(selectquery);
						
			// retrieve result set
			rs = stmnt.executeQuery();
			rsmd = (ResultSetMetaData) rs.getMetaData();
			columnCount = rsmd.getColumnCount();
			
			// process result set
			while(rs.next()){
				ro = new JSONObject();
				for(int i=1;i<=columnCount;i++){
					result = rs.getString(i);
					columnName = rsmd.getColumnName(i);
					// setup resulting JSON Object
					ro.put(columnName, result);
					
				}
				qs.add(ro);
			}
			if (qs.isEmpty()){
				result = "No results";
				
				// return HTTP Response on error
				HttpResponse er = new HttpResponse(result);
				er.setStatus(404);
				return er;
								
			} else {
				// return HTTP Response on success
				HttpResponse r = new HttpResponse(qs.toJSONString());
				r.setStatus(200);
				return r;
				}
		} catch (Exception e) {
			// return HTTP Response on error
			HttpResponse er = new HttpResponse("Internal error: " + e.getMessage() + stmnt.toString());
			er.setStatus(500);
			return er;
		} finally {
			// free resources
			if (rs != null) {
				try {
					rs.close();
				} catch (Exception e) {
					Context.logError(this, e.getMessage());
					
					// return HTTP Response on error
					HttpResponse er = new HttpResponse("Internal error: " + e.getMessage() + stmnt.toString());
					er.setStatus(500);
					return er;
				}
			}
			if (stmnt != null) {
				try {
					stmnt.close();
				} catch (Exception e) {
					Context.logError(this, e.getMessage());
					
					// return HTTP Response on error
					HttpResponse er = new HttpResponse("Internal error: " + e.getMessage());
					er.setStatus(500);
					return er;
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {
					Context.logError(this, e.getMessage());
					
					// return HTTP Response on error
					HttpResponse er = new HttpResponse("Internal error: " + e.getMessage());
					er.setStatus(500);
					return er;
				}
			}
		}
	}
	/**
	 * Method to update the  url and uploader of a video with a given id
	 * @param videoid id of the video
	 * @param data json data
	 * @return HttpResponse with the result of the method
	 */
	
	@POST
	@Path("videos/{videoid}")
	@Summary("update details for an existing video.")
	@Notes("Requires authentication.")
	@ApiResponses(value={
			@ApiResponse(code = 200, message = "Video details updated successfully."),
			@ApiResponse(code = 401, message = "User is not authenticated."),
			@ApiResponse(code = 404, message = "Video not found."),
			@ApiResponse(code = 500, message = "Internal error.")	
	})
	public HttpResponse setVideoDetail(@PathParam("videoid") String videoid, @ContentParam String data) {
		
		String result = "";
		String updateStr = "";
		Connection conn = null;
		PreparedStatement stmnt = null;
		ResultSet rs = null;
		try {
			JSONObject o;
			try{	
				o = (JSONObject) JSONValue.parseWithException(data);
			} catch (ParseException e1) {
				throw new IllegalArgumentException("data is not valid JSON!");
			}
			conn = dbm.getConnection();
									
			for (Object key: o.keySet()){
				if(updateStr.equals("")){
					updateStr+= key + "  =  '" + o.get(key) +  "' ";
				}else{
					updateStr+= ", " + key + "  =  '" + o.get(key)  +  "' ";
				}
			}
			
			String str = ((UserAgent) getActiveAgent()).getLoginName();
			System.out.println(str);
			if(getActiveAgent().getId() != getActiveNode().getAnonymous().getId()){
				updateStr= "UPDATE videodetails SET " + updateStr + " WHERE videoid = " + videoid +";";
				stmnt = conn.prepareStatement(updateStr);
				int rows = stmnt.executeUpdate();
				if(rows>0){
					result = "Database updated. " + rows + " row(s) affected";
					
					// return 
					HttpResponse r = new HttpResponse(result);
					r.setStatus(200);
					return r;
				}else{
					result = "No video found";
					
					// return 
					HttpResponse r = new HttpResponse(result);
					r.setStatus(404);
					return r;
					
				}				
			}else{
				result = "User in not authenticated";
				
				// return 
				HttpResponse r = new HttpResponse(result);
				r.setStatus(401);
				return r;
				
			}
			
		} catch (Exception e) {
			// return HTTP Response on error
			HttpResponse er = new HttpResponse("Internal error: " + e.getMessage());
			er.setStatus(500);
			return er;
		} finally {
			// free resources if exception or not
			if (rs != null) {
				try {
					rs.close();
				} catch (Exception e) {
					Context.logError(this, e.getMessage());
					
					// return HTTP Response on error
					HttpResponse er = new HttpResponse("Internal error: " + e.getMessage());
					er.setStatus(500);
					return er;
				}
			}
			if (stmnt != null) {
				try {
					stmnt.close();
				} catch (Exception e) {
					Context.logError(this, e.getMessage());
					
					// return HTTP Response on error
					HttpResponse er = new HttpResponse("Internal error: " + e.getMessage());
					er.setStatus(500);
					return er;
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {
					Context.logError(this, e.getMessage());
					
					// return HTTP Response on error
					HttpResponse er = new HttpResponse("Internal error: " + e.getMessage());
					er.setStatus(500);
					return er;
				}
			}
		}
	}
	
	/**
	 * 
	 * @param data video details that need to be saved. The data come in a JSON format
	 * @return HttpResponse 
	 */
	
	@POST
	@Path("videos")
	@Summary("Insert new video")
	@Notes("Requires authentication.")
	@ApiResponses(value={
			@ApiResponse(code = 200, message = "Video details saved successfully."),
			@ApiResponse(code = 401, message = "User is not authenticated."),
			@ApiResponse(code = 409, message = "Video already exists."),
			@ApiResponse(code = 500, message = "Internal error.")	
	})
	public HttpResponse addNewVideo( @ContentParam String data){
		
		String result = "";
		Connection conn = null;
		PreparedStatement stmnt = null;
		ResultSet rs = null;
		java.util.Date date= new java.util.Date();
		try {
			JSONObject o;
			try{	
				o = (JSONObject) JSONValue.parseWithException(data);
			} catch (ParseException e1) {
				throw new IllegalArgumentException("data is not valid JSON!");
			}
			if(getActiveAgent().getId() != getActiveNode().getAnonymous().getId()){
				for (Object key: o.keySet()){
					result+= key + " " + o.get(key);
				}
				conn = dbm.getConnection();
				stmnt = conn.prepareStatement("select videoId from videodetails  WHERE videoid = ?");
				stmnt.setString(1, (String) o.get("videoid"));
				rs = stmnt.executeQuery(); 
				if(!rs.isBeforeFirst())
				{
					rs.close();
					stmnt.close();
					conn.close();
					conn = null;
					conn = dbm.getConnection();
					PreparedStatement preparedStatement = null;
					preparedStatement = conn.prepareStatement("INSERT INTO videodetails(videoId, url, thumbnail, uploader, tool, community, time, description)"
							+ "					 VALUES (?,?,?,?,?,?,?,?);");
					preparedStatement.setString(1, (String) o.get("videoid"));
					preparedStatement.setString(2, (String) o.get("url"));
					preparedStatement.setString(3, (String) o.get("thumbnail"));
					preparedStatement.setString(4, (String) o.get("uploader"));
					preparedStatement.setString(5, (String) o.get("tool"));
					preparedStatement.setString(6, (String) o.get("community"));
					preparedStatement.setString(7, new Timestamp(date.getTime()).toString());
					preparedStatement.setString(8, (String) o.get("description"));
					int rows = preparedStatement.executeUpdate(); 
					result = "Row(s) inserted. " + rows + " row(s) affected.";
					
					// return 
					HttpResponse r = new HttpResponse(result);
					r.setStatus(200);
					return r;
				}else{
					result = "Video already exists.";
					
					// return 
					HttpResponse r = new HttpResponse(result);
					r.setStatus(409);
					return r;
					
				}
			}else{
				result = "User in not authenticated";
				
				// return 
				HttpResponse r = new HttpResponse(result);
				r.setStatus(401);
				return r;		
			}
			
		} catch (Exception e) {
			// return HTTP Response on error
			HttpResponse er = new HttpResponse("Internal error: " + e.getMessage());
			er.setStatus(500);
			return er;
		} finally {
			// free resources if exception or not
			if (rs != null) {
				try {
					rs.close();
				} catch (Exception e) {
					Context.logError(this, e.getMessage());
					
					// return HTTP Response on error
					HttpResponse er = new HttpResponse("Internal error: " + e.getMessage());
					er.setStatus(500);
					return er;
				}
			}
			if (stmnt != null) {
				try {
					stmnt.close();
				} catch (Exception e) {
					Context.logError(this, e.getMessage());
					
					// return HTTP Response on error
					HttpResponse er = new HttpResponse("Internal error: " + e.getMessage());
					er.setStatus(500);
					return er;
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {
					Context.logError(this, e.getMessage());
					
					// return HTTP Response on error
					HttpResponse er = new HttpResponse("Internal error: " + e.getMessage());
					er.setStatus(500);
					return er;
				}
			}
		}
	}
	
	
	
	/**
	 * Method to delete details for one video 
	 * 
	 * @param videoid id of the video to be deleted
	 * @return HttpResponse	 
	 */
	
	@DELETE
	@Path("videos/{videoId}")
	@Summary("Delete video details")
	@Notes("Requires authentication.")
	@ApiResponses(value={
			@ApiResponse(code = 200, message = "Video details deleted successfully."),
			@ApiResponse(code = 401, message = "User is not authenticated."),
			@ApiResponse(code = 404, message = "Video not found."),
			@ApiResponse(code = 500, message = "Internal error.")	
	})
	public HttpResponse deleteVideo(@PathParam("videoId") String videoid ) {
		
		String result = "";
		Connection conn = null;
		PreparedStatement stmnt = null;
		ResultSet rs = null;
		try {
			if(getActiveAgent().getId() != getActiveNode().getAnonymous().getId()){
				conn = dbm.getConnection();
				stmnt = conn.prepareStatement("DELETE FROM videodetails WHERE videoId = ?;");
				stmnt.setString(1, videoid);
				int rows = stmnt.executeUpdate(); 
				if(rows>0){
				result = "Row(s) deleted. " + rows + " row(s) affected";
				
				// return 
				HttpResponse r = new HttpResponse(result);
				r.setStatus(200);
				return r;
				}else{
					result = "No video found";
					
					// return 
					HttpResponse r = new HttpResponse(result);
					r.setStatus(404);
					return r;
					}
			}else{
				result = "User in not authenticated";
				
				// return 
				HttpResponse r = new HttpResponse(result);
				r.setStatus(401);
				return r;
				
			}
			
		} catch (Exception e) {
			// return HTTP Response on error
			HttpResponse er = new HttpResponse("Internal error: " + e.getMessage());
			er.setStatus(500);
			return er;
		} finally {
			// free resources if exception or not
			if (rs != null) {
				try {
					rs.close();
				} catch (Exception e) {
					Context.logError(this, e.getMessage());
					
					// return HTTP Response on error
					HttpResponse er = new HttpResponse("Internal error: " + e.getMessage());
					er.setStatus(500);
					return er;
				}
			}
			if (stmnt != null) {
				try {
					stmnt.close();
				} catch (Exception e) {
					Context.logError(this, e.getMessage());
					
					// return HTTP Response on error
					HttpResponse er = new HttpResponse("Internal error: " + e.getMessage());
					er.setStatus(500);
					return er;
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {
					Context.logError(this, e.getMessage());
					
					// return HTTP Response on error
					HttpResponse er = new HttpResponse("Internal error: " + e.getMessage());
					er.setStatus(500);
					return er;
				}
			}
		}
	}
	
	
	// ================= Swagger Resource Listing & API Declarations =====================

		@GET
		@Path("api-docs")
		@Summary("retrieve Swagger 1.2 resource listing.")
		@ApiResponses(value={
				@ApiResponse(code = 200, message = "Swagger 1.2 compliant resource listing"),
				@ApiResponse(code = 404, message = "Swagger resource listing not available due to missing annotations."),
		})
		@Produces(MediaType.APPLICATION_JSON)
		public HttpResponse getSwaggerResourceListing(){
			return RESTMapper.getSwaggerResourceListing(this.getClass());
		}

		@GET
		@Path("api-docs/{tlr}")
		@Produces(MediaType.APPLICATION_JSON)
		@Summary("retrieve Swagger 1.2 API declaration for given top-level resource.")
		@ApiResponses(value={
				@ApiResponse(code = 200, message = "Swagger 1.2 compliant API declaration"),
				@ApiResponse(code = 404, message = "Swagger API declaration not available due to missing annotations."),
		})
		public HttpResponse getSwaggerApiDeclaration(@PathParam("tlr") String tlr){
			return RESTMapper.getSwaggerApiDeclaration(this.getClass(),tlr, epUrl);
		}

	/**
	 * Method for debugging purposes.
	 * Here the concept of restMapping validation is shown.
	 * It is important to check, if all annotations are correct and consistent.
	 * Otherwise the service will not be accessible by the WebConnector.
	 * Best to do it in the unit tests.
	 * To avoid being overlooked/ignored the method is implemented here and not in the test section.
	 * @return  true, if mapping correct
	 */
	public boolean debugMapping() {
		String XML_LOCATION = "./restMapping.xml";
		String xml = getRESTMapping();

		try {
			RESTMapper.writeFile(XML_LOCATION, xml);
		} catch (IOException e) {
			e.printStackTrace();
		}

		XMLCheck validator = new XMLCheck();
		ValidationResult result = validator.validate(xml);

		if (result.isValid())
			return true;
		return false;
	}

	/**
	 * This method is needed for every RESTful application in LAS2peer. There is no need to change!
	 * 
	 * @return the mapping
	 */
	public String getRESTMapping() {
		String result = "";
		try {
			result = RESTMapper.getMethodsAsXML(this.getClass());
		} catch (Exception e) {

			e.printStackTrace();
		}
		return result;
	}

}
