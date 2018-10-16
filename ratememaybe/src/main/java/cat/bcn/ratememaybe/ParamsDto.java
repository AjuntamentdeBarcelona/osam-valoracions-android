package cat.bcn.ratememaybe;


import com.google.gson.annotations.SerializedName;

import java.util.List;

class ParamsDto {

    /* Nº de dies mínims que han de passar per que l’app mostri la popup */
    @SerializedName("tmin")
    public int tmin;

    /* nº de vegades que ha cal obrir l’app per tal que es mostri la popup (si s’ha superat Tmin) */
    @SerializedName("num_apert")
    public int numApert;

    /* text que es mostrarà al popup */
    @SerializedName("messages")
    public List<Message> messages;

}
