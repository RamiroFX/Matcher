package com.matcher.matcher.entities;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Ramiro on 04/02/2018.
 */

public class Groups {
    private String uid;
    private String name;
    private String description;
    private Date creationDate;
    private Users creator;
    private double  ubicacionLatitud;
    private double  ubicacionLongitud;
    private ArrayList<Users> usersMembers;
}
