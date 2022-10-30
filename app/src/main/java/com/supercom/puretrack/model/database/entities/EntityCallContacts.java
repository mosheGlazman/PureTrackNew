package com.supercom.puretrack.model.database.entities;

public class EntityCallContacts extends DatabaseEntity {
    public int Type;
    public String Number;

    public EntityCallContacts(int Type, String Number) {
        this.Type = Type;
        this.Number = Number;
    }

}
