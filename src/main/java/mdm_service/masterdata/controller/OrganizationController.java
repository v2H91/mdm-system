package mdm_service.masterdata.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import mdm_service.masterdata.dto.request.OrganizationRequest;
import mdm_service.masterdata.dto.response.OrganizationResponse;
import mdm_service.masterdata.service.OrganizationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/organizations")
@Tag(name = "Organization Management", description = "Quản lý dữ liệu Master cho Tổ chức")
public class OrganizationController {

    private final OrganizationService orgService;

    public OrganizationController(OrganizationService orgService) {
        this.orgService = orgService;
    }

    @PostMapping
    @Operation(summary = "Tạo mới tổ chức", description = "Hệ thống sẽ validate dựa trên regex trong DB")
    public ResponseEntity<OrganizationResponse> create(@RequestBody OrganizationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orgService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrganizationResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(orgService.getById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        orgService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
