package service.cloud.request.clientRequest.config;

import com.google.gson.*;
import service.cloud.request.clientRequest.dto.dto.TransacctionDTO;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransacctionDTODeserializer implements JsonDeserializer<TransacctionDTO> {

    private final Gson cleanGson;

    public TransacctionDTODeserializer() {
        // Usa la limpieza general para todos los strings
        this.cleanGson = new GsonBuilder()
                .registerTypeAdapterFactory(new StringCleaningTypeAdapterFactory())
                .create();
    }

    @Override
    public TransacctionDTO deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {

        // Deserializa todo el objeto con limpieza general
        TransacctionDTO dto = cleanGson.fromJson(json, TransacctionDTO.class);

        JsonObject jsonObject = json.getAsJsonObject();

        // Campos que deben preservar saltos de línea (sin limpiar)
        preservarCampoOriginal(jsonObject, dto, "FE_Comentario");
        preservarCampoOriginal(jsonObject, dto, "RazonSocial");
        preservarCampoOriginal(jsonObject, dto, "SN_RazonSocial");
        preservarCampoOriginal(jsonObject, dto, "DIR_Direccion");
        preservarCampoOriginal(jsonObject, dto, "SN_DIR_Direccion");
        preservarCampoOriginal(jsonObject, dto, "DIR_NomCalle");
        preservarCampoOriginal(jsonObject, dto, "SN_DIR_NomCalle");
        preservarCampoOriginal(jsonObject, dto, "PersonContacto");
        preservarCampoOriginal(jsonObject, dto, "NombreComercial");
        preservarCampoOriginal(jsonObject, dto, "SN_NombreComercial");
        preservarCampoOriginal(jsonObject, dto, "Observacione");

        // Preservar saltos de línea en transactionContractDocRefListDTOS
        if (jsonObject.has("transactionContractDocRefListDTOS") && !jsonObject.get("transactionContractDocRefListDTOS").isJsonNull()) {
            JsonArray contractArray = jsonObject.getAsJsonArray("transactionContractDocRefListDTOS");
            List<Map<String, String>> contractList = new ArrayList<>();

            for (JsonElement contractElement : contractArray) {
                if (contractElement.isJsonObject()) {
                    JsonObject contractObj = contractElement.getAsJsonObject();
                    Map<String, String> contractMap = new HashMap<>();

                    // Iterar sobre cada campo del mapa y preservar los valores originales (con saltos de línea)
                    for (Map.Entry<String, JsonElement> entry : contractObj.entrySet()) {
                        String key = entry.getKey();
                        JsonElement value = entry.getValue();

                        if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()) {
                            // Obtener el valor original sin limpieza
                            contractMap.put(key, value.getAsString());
                        } else if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isNumber()) {
                            contractMap.put(key, value.getAsString());
                        } else if (value.isJsonNull()) {
                            contractMap.put(key, null);
                        } else {
                            contractMap.put(key, value.toString());
                        }
                    }

                    contractList.add(contractMap);
                }
            }

            dto.setTransactionContractDocRefListDTOS(contractList);
        }

        // Preservar saltos de línea en descripciones de líneas
        if (jsonObject.has("transactionLineasDTOList") && !jsonObject.get("transactionLineasDTOList").isJsonNull()) {
            JsonArray lineasArray = jsonObject.getAsJsonArray("transactionLineasDTOList");

            // Iterar sobre las líneas ya deserializadas y actualizar las descripciones con sus valores originales
            if (dto.getTransactionLineasDTOList() != null && dto.getTransactionLineasDTOList().size() == lineasArray.size()) {
                for (int i = 0; i < lineasArray.size(); i++) {
                    JsonObject lineaObj = lineasArray.get(i).getAsJsonObject();

                    // Preservar Descripcion
                    if (lineaObj.has("Descripcion") && !lineaObj.get("Descripcion").isJsonNull()) {
                        dto.getTransactionLineasDTOList().get(i).setDescripcion(lineaObj.get("Descripcion").getAsString());
                    }

                    // Preservar campos de usuario en líneas
                    if (lineaObj.has("transaccionLineasCamposUsuario") && !lineaObj.get("transaccionLineasCamposUsuario").isJsonNull()) {
                        JsonObject camposUsuarioObj = lineaObj.getAsJsonObject("transaccionLineasCamposUsuario");
                        Map<String, String> camposUsuarioMap = new HashMap<>();

                        for (Map.Entry<String, JsonElement> entry : camposUsuarioObj.entrySet()) {
                            String key = entry.getKey();
                            JsonElement value = entry.getValue();

                            if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()) {
                                camposUsuarioMap.put(key, value.getAsString());
                            } else if (!value.isJsonNull()) {
                                camposUsuarioMap.put(key, value.toString());
                            }
                        }

                        dto.getTransactionLineasDTOList().get(i).setTransaccionLineasCamposUsuario(camposUsuarioMap);
                    }
                }
            }
        }

        return dto;
    }

    /**
     * Método auxiliar para preservar el valor original de un campo (con saltos de línea)
     */
    private void preservarCampoOriginal(JsonObject jsonObject, TransacctionDTO dto, String fieldName) {
        if (jsonObject.has(fieldName) && !jsonObject.get(fieldName).isJsonNull()) {
            String valorOriginal = jsonObject.get(fieldName).getAsString();

            // Usar reflection para setear el valor
            try {
                String setterName = "set" + fieldName;
                dto.getClass().getMethod(setterName, String.class).invoke(dto, valorOriginal);
            } catch (Exception e) {
                // Si no existe el setter, ignorar silenciosamente
            }
        }
    }
}

