package fi.oulu.acp.communityreminder;

/**
 * Created by alex on 5.4.2015.
 */
public class Notification {
    private String phoneNumber;
    private String title;
    private String message;

    public Notification(){

    }

    public Notification(String phoneNumber, String title, String message){
        this.phoneNumber = phoneNumber;
        this.title = title;
        this.message = message;
    }

    public String getPhoneNumber(){
        return phoneNumber;
    }

    public String getTitle(){
        return title;
    }

    public String getMessage(){
        return message;
    }

    public void setPhoneNumber (String phoneNumber){
        this.phoneNumber = phoneNumber;
    }

    public void setTitle (String title){
        this.title = title;
    }

    public void setMessage(String message){
        this.message = message;
    }
}
