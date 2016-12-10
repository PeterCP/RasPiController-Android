package dev.petercp.raspicontroller.exceptions;

import android.database.SQLException;

public class SQLQueryException extends SQLException {

    public SQLQueryException(String s) {
        super(s);
    }

}
