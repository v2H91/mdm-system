package mdm_service.masterdata.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import mdm_service.masterdata.listener.AddressListener;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "addresses")
@Getter @Setter
@EntityListeners(AddressListener.class)
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String houseNumber;
    private String street;

    // Chỉ cần một tham chiếu đến cấp hành chính thấp nhất (Xã/Phường)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_code", referencedColumnName = "code")
    private Location location;

    private String fullAddress;

    @PrePersist
    @PreUpdate
    public void generateFullAddress() {
        List<String> parts = new ArrayList<>();

        // 1. Thêm Số nhà và Tên đường
        if (houseNumber != null && !houseNumber.isBlank()) parts.add(houseNumber.trim());
        if (street != null && !street.isBlank()) parts.add(street.trim());

        // 2. Duyệt ngược từ Location hiện tại lên các cấp cha
        if (this.location != null) {
            Location current = this.location;
            while (current != null) {
                if (current.getName() != null && !current.getName().isBlank()) {
                    parts.add(current.getName().trim());
                }
                current = current.getParent(); // Leo lên cấp cha (Quận -> Tỉnh)
            }
        }

        // 3. Nối các phần bằng dấu phẩy và dọn dẹp khoảng trắng
        this.fullAddress = parts.stream()
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining(", "));
    }
}