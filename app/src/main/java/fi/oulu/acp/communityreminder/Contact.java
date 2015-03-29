package fi.oulu.acp.communityreminder;

import android.graphics.Bitmap;

import java.util.ArrayList;

/**
 * Created by JuanCamilo on 3/19/2015.
 */
public class Contact {

    private String name;
    private String id;
    private ArrayList<String> phones;
    private Bitmap picture;
    private String birthday;
    private int stepGoals;


    public Contact(String contactId, ArrayList<String> contactPhones){
        id = contactId;
        phones = contactPhones;
    }

    public Contact(String contactId, ArrayList<String> contactPhones, String contactName){
        id = contactId;
        phones = contactPhones;
        name = contactName;
    }

    public Contact(String contactId, ArrayList<String> contactPhones, String contactName, Bitmap contactPicture){
        id = contactId;
        phones = contactPhones;
        name = contactName;
        picture = contactPicture;
    }



    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ArrayList<String> getPhones() {
        return phones;
    }

    public void setPhones(ArrayList<String> phones) {
        this.phones = phones;
    }

    public Bitmap getPicture() {
        return picture;
    }

    public void setPicture(Bitmap picture) {
        this.picture = picture;
    }

    public void setBirthday(String sBirthday){
        birthday = sBirthday;
    }

    public void setStepGoals(int sStepGoals){
        stepGoals = sStepGoals;
    }
}
