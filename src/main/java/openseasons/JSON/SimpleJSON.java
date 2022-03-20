package openseasons.JSON;

import com.google.gson.JsonObject;
import net.fabricmc.loader.impl.lib.gson.JsonReader;
import net.fabricmc.loader.impl.lib.gson.JsonToken;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;


public class SimpleJSON {
    /**
     * Saves the JsonObject to a file in the specified path.
     * @param path
     * @param json
     * @throws IOException
     */
    public static void saveTo(String path, JsonObject json) throws IOException{
        try(FileWriter writer = new FileWriter(path)){
            writer.write(json.toString());
        }
    }

    /**
     * Retrieves JsonObject from the specified path and returns it.
     * @param path
     * @return
     * @throws java.io.IOException
     * @throws IllegalStateException
     * @throws NumberFormatException
     */
    public static JsonObject loadFrom(String path) throws java.io.IOException, IllegalStateException, NumberFormatException {
        JsonObject jsonObject = new JsonObject();
        try(FileReader reader = new FileReader(path)){
            jsonObject = load(reader);
        }

        return  jsonObject;
    }

    private static JsonObject load(Reader reader) throws java.io.IOException, IllegalStateException, NumberFormatException{
        JsonObject jsonObject = new JsonObject();

        String string = null;
        Integer integer = null;
        String name = null;

        JsonReader jsonReader = new JsonReader(reader);
        jsonReader.beginObject();
        while (jsonReader.hasNext()){
            JsonToken token = jsonReader.peek();

            if (token == JsonToken.NAME){
                name = jsonReader.nextName();
            }else if (token == JsonToken.NUMBER){
                integer = jsonReader.nextInt();

                jsonObject.addProperty(name, integer);//no java, 'name' may not be null. But I will not waste my time
                // explaining to you why that is the case.

            }else if (token == JsonToken.STRING){
                string = jsonReader.nextString();

                jsonObject.addProperty(name, string);

            }
        }
        jsonReader.endObject();
        return jsonObject;
    }
}