package back.tesis.modules.profile;

import back.tesis.dto.profile.ProfileResponse;
import back.tesis.security.TenantContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/v1/{empresaId}/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public ProfileResponse getProfile(@PathVariable String empresaId) {
        return profileService.getProfile(usuarioActual());
    }

    @PutMapping("/foto")
    public ProfileResponse updateFoto(
            @PathVariable String empresaId,
            @RequestParam("file") MultipartFile file
    ) {
        return profileService.updateFoto(usuarioActual(), file);
    }

    @DeleteMapping("/foto")
    public ResponseEntity<Void> deleteFoto(@PathVariable String empresaId) {
        profileService.deleteFoto(usuarioActual());
        return ResponseEntity.noContent().build();
    }

    private String usuarioActual() {
        return TenantContext.get().usuarioId();
    }
}
