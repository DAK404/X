/*
* ---------------!DISCLAIMER!--------------- *
*                                            *
*         THIS CODE IS RELEASE READY         *
*                                            *
*  THIS CODE HAS BEEN CHECKED, REVIEWED AND  *
*   TESTED. THIS CODE HAS NO KNOWN ISSUES.   *
*    PLEASE REPORT OR OPEN A NEW ISSUE ON    *
*     GITHUB IF YOU FIND ANY PROBLEMS OR     *
*              ERRORS IN THE CODE.           *
*                                            *
*   THIS CODE FALLS UNDER THE LGPL LICENSE.  *
*    YOU MUST INCLUDE THIS DISCLAIMER WHEN   *
*        DISTRIBUTING THE SOURCE CODE.       *
*   (SEE LICENSE FILE FOR MORE INFORMATION)  *
*                                            *
* ------------------------------------------ *
*/

package Truncheon.API.Dragon;

//Import the required Java SQL classes
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;


/**
* 
*/
public final class LoginAPI
{
    //a universal string to read the file

    private String _User;
    private String _Pass;
    private String _SecKey;

    private String url = "jdbc:sqlite:./System/Private/Truncheon/mud.db";

    /**
    *
    * @param Us
    * @param Pa
    * @param SK
    * @throws Exception : Handle exceptions thrown during program runtime.
    */
    public LoginAPI(String Us, String Pa, String SK)throws Exception
    {
        _User = Us;
        _Pass = Pa;
        _SecKey = SK;

        Class.forName("org.sqlite.JDBC");

    }

    /**
    *
    * @return
    * @throws Exception : Handle exceptions thrown during program runtime.
    */
    public final boolean status()throws Exception
    {
        try
        {
            return checkDetails();
        }
        catch(Exception E)
        {
            E.printStackTrace();
            return false;
        }
    }

    /**
    *
    * @return
    * @throws Exception : Handle exceptions thrown during program runtime.
    */
    private final boolean checkDetails()throws Exception
    {
        Connection conn = null;
        boolean loginStatus = false;
        try
        {
            conn = DriverManager.getConnection(url);
            String sql = "SELECT Username, Password, SecurityKey FROM FCAD WHERE Username = ? AND Password = ? AND SecurityKey = ?;";

            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, _User);
            pstmt.setString(2, _Pass);
            pstmt.setString(3, _SecKey);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next())
            if (rs.getString("Username").equals(_User) & rs.getString("Password").equals(_Pass) & rs.getString("SecurityKey").equals(_SecKey))
            loginStatus = true;

            if(! loginStatus)
            System.out.println("Incorrect Credentials, Please try again.");

            rs.close();
            conn.close();

            System.gc();

            return loginStatus;
        }
        catch (Exception E)
        {
            E.printStackTrace();
            System.out.println("[ ATTENTION ] : Incorrect Credentials. Please check details and try again.");
            return false;
        }
    }
}