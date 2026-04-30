package service.cloud.request.clientRequest.config;

import org.modelmapper.ModelMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Named;

@ApplicationScoped
public class MapperConfig {

    @Produces
    @Named("defaultMapper")
    public ModelMapper defaultMapper() {
        return new ModelMapper();
    }

}
