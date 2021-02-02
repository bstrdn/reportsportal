package ru.bstrdn.report.gate.configurations;

import com.healthmarketscience.jackcess.CryptCodecProvider;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.DatabaseBuilder;
import net.ucanaccess.jdbc.JackcessOpenerInterface;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

public class CryptCodecOpener implements JackcessOpenerInterface {

    public Database open(File fl, String pwd) throws IOException {
        DatabaseBuilder dbd = new DatabaseBuilder(fl);
        dbd.setAutoSync(false);
        dbd.setCodecProvider(new CryptCodecProvider(pwd));
        dbd.setReadOnly(true);
        dbd.setCharset(Charset.forName("WINDOWS-1251"));
        return dbd.open();
    }
    // просмотрщик .mdb https://mdbviewer.herokuapp.com/
    // https://stackoverflow.com/questions/59714917/charset-for-ms-access-97-db-using-ucanaccess/65991025#65991025
    // Notice that the parameter setting AutoSync=false is recommended with UCanAccess for performance reasons.
    // UCanAccess flushes the updates to disk at transaction end.
    // For more details about autosync parameter (and related tradeoff), see the Jackcess documentation.
}