package mdm_service.masterdata.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import mdm_service.masterdata.dto.request.AddressRequest;
import mdm_service.masterdata.service.AddressService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/organizations/{orgId}/addresses")
@Tag(name = "Organization Address API", description = "Quản lý địa chỉ cho từng tổ chức")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @PostMapping
    @Operation(summary = "Thêm địa chỉ mới cho tổ chức")
    public ResponseEntity<Void> addAddress(@RequestBody AddressRequest request) {
        addressService.addAddressToOrg(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // TODO: Viết API GET để lấy danh sách địa chỉ của Org kèm theo mapping (Local/Global)
}
