package mdm_service.masterdata.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import mdm_service.masterdata.document.AddressDocument;
import mdm_service.masterdata.document.OrganizationDocument;
import mdm_service.masterdata.dto.request.AddressRequest;
import mdm_service.masterdata.service.AddressSearchService;
import mdm_service.masterdata.service.AddressService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/addresses")
@Tag(name = "Address API", description = "Quản lý địa chỉ")
public class AddressController {

    private final AddressService addressService;
    private final AddressSearchService searchService;
    public AddressController(AddressService addressService,AddressSearchService searchService) {
        this.addressService = addressService;
        this.searchService = searchService;

    }

    @PostMapping
    @Operation(summary = "Thêm địa chỉ mới")
    public ResponseEntity<Void> addAddress(@RequestBody AddressRequest request) {
        addressService.addAddress(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<AddressDocument>> search(@RequestParam("q") String query) {
        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(searchService.search(query));
    }
    // TODO: Viết API GET để lấy danh sách địa chỉ của Org kèm theo mapping (Local/Global)
}
