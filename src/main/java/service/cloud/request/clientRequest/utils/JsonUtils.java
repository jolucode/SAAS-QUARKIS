package service.cloud.request.clientRequest.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import service.cloud.request.clientRequest.config.StringCleaningTypeAdapterFactory;


import service.cloud.request.clientRequest.config.TransacctionDTODeserializer;
import service.cloud.request.clientRequest.dto.dto.TransacctionDTO;

import com.google.gson.*;

import java.lang.reflect.Type;

public class JsonUtils {

    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapterFactory(new StringCleaningTypeAdapterFactory()) // limpieza general
            .registerTypeAdapter(TransacctionDTO.class, new TransacctionDTODeserializer()) // excepción FE_Comentario
            .registerTypeAdapter(TransacctionDTO[].class, new JsonDeserializer<TransacctionDTO[]>() {
                @Override
                public TransacctionDTO[] deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                        throws JsonParseException {
                    JsonArray jsonArray = json.getAsJsonArray();
                    TransacctionDTO[] array = new TransacctionDTO[jsonArray.size()];
                    TransacctionDTODeserializer dtoDeserializer = new TransacctionDTODeserializer();

                    for (int i = 0; i < jsonArray.size(); i++) {
                        array[i] = dtoDeserializer.deserialize(jsonArray.get(i), TransacctionDTO.class, context);
                    }

                    return array;
                }
            })
            .create();

    // Serializa un objeto a JSON (sin limpieza)
    public static String toJson(Object obj) {
        return gson.toJson(obj);
    }

    // Deserializa un objeto desde JSON
    public static <T> T fromJson(String json, Class<T> clazz) {
        return gson.fromJson(json, clazz);
    }
}


