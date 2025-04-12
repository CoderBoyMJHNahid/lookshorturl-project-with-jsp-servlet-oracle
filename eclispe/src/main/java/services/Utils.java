package services;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Random;
import java.nio.charset.StandardCharsets;
public class Utils {
	private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int SHORT_URL_LENGTH = 6;

	 public String hashPassword(String password) throws NoSuchAlgorithmException {
	        MessageDigest md = MessageDigest.getInstance("SHA-256");
	        byte[] hashBytes = md.digest(password.getBytes(StandardCharsets.UTF_8));
	        StringBuilder sb = new StringBuilder();
	        for (byte b : hashBytes) {
	            sb.append(String.format("%02x", b));
	        }
	        return sb.toString();
	    }
	 
	 public String generateShortCode() {
	        Random random = new Random();
	        StringBuilder sb = new StringBuilder(SHORT_URL_LENGTH);
	        
	        for (int i = 0; i < SHORT_URL_LENGTH; i++) {
	            sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
	        }
	        
	        return sb.toString();
	    }
	 
	 public String buildErrorJson(Map<String, String> errors) {
	        StringBuilder json = new StringBuilder("{\"success\": false, \"errors\": {");
	        int count = 0;
	        for (Map.Entry<String, String> entry : errors.entrySet()) {
	            if (count++ > 0) json.append(",");
	            json.append("\"").append(entry.getKey()).append("\": \"")
	                .append(entry.getValue()).append("\"");
	        }
	        json.append("}}");
	        return json.toString();
	    }

	 public boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email.matches(emailRegex);
    }
	 
}
