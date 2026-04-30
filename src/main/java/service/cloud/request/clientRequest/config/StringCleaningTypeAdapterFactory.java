package service.cloud.request.clientRequest.config;

import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Adaptador personalizado para limpiar automáticamente los campos String
 * al deserializar objetos desde JSON con Gson.
 */
public class StringCleaningTypeAdapterFactory implements TypeAdapterFactory {

    // Elimina saltos de línea (\r, \n) y espacios múltiples
    private static final Pattern CLEAN_PATTERN = Pattern.compile("\\r?\\n");

    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        if (!String.class.equals(type.getRawType())) {
            return null; // Solo aplica este adaptador a campos de tipo String
        }

        return (TypeAdapter<T>) new TypeAdapter<String>() {

            @Override
            public void write(JsonWriter out, String value) throws IOException {
                out.value(value); // No se modifica en serialización
            }

            @Override
            public String read(JsonReader in) throws IOException {
                String value = in.nextString();
                if (value == null) return null;

                // Limpieza: reemplaza saltos de línea por espacio, elimina espacios múltiples y recorta extremos
                return value
                        .replaceAll("[\\r\\n]", " ")     // Reemplaza \r y \n por espacio
                        .replaceAll("\\s{2,}", " ")      // Colapsa espacios múltiples
                        .trim();                      // Quita espacios al inicio y fin
            }
        };
    }
}
