package moz.http;
import java.util.Hashtable;

public class HttpData {
      public String content;
      public Hashtable<Object, String> cookies = new Hashtable<Object, String>();
      public Hashtable<Object, String> headers = new Hashtable<Object, String>();
}
