package cat.bcn.ratememaybe;

import com.google.gson.annotations.SerializedName;

public class Message {

    /* missage languaje */
    @SerializedName("languaje")
    public String languaje;

    /* message content */
    @SerializedName("content")
    public String content;
}
