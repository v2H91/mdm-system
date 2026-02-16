package mdm_service.masterdata.repository;

import mdm_service.masterdata.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    // TODO: Thêm phương thức tìm kiếm địa chỉ theo street hoặc fullAddress (Fuzzy search)
    List<Address> findByFullAddressContainingIgnoreCase(String keyword);
}