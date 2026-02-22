package mdm_service.masterdata.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mdm_service.masterdata.constant.JobStatus;
import mdm_service.masterdata.document.AddressDocument;
import mdm_service.masterdata.entity.Address;
import mdm_service.masterdata.entity.BatchJob;
import mdm_service.masterdata.entity.Organization;
import mdm_service.masterdata.repository.AddressRepository;
import mdm_service.masterdata.repository.JobRepository;
import mdm_service.masterdata.repository.OrganizationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReIndexService {

    private final OrganizationRepository orgRepository;
    private final OrganizationSearchService searchService;

    private final AddressRepository addressRepository;
    private final AddressSearchService addressSearchService;

    private final ElasticsearchOperations esOperations;
    private final JobRepository jobRepository;

    @Async // Chạy ngầm để không block API
    public void reIndexOrganizations(String jobId) {
        IndexOperations indexOps = esOperations.indexOps(AddressDocument.class);
        indexOps.delete();
        indexOps.create();

        BatchJob job = jobRepository.findById(jobId).orElseThrow();
        job.setStatus(JobStatus.RUNNING);
        job.setStartTime(LocalDateTime.now());
        jobRepository.save(job);

        try {
            int pageSize = 100;
            Pageable pageable = PageRequest.of(0, pageSize);
            Page<Organization> page;
            int processed = 0;

            // Đếm tổng số bản ghi
            long total = orgRepository.count();
            job.setTotalItems((int) total);

            do {
                page = orgRepository.findAll(pageable);
                searchService.bulkIndexOrganizations(page.getContent());

                processed += page.getContent().size();
                job.setProcessedItems(processed);
                jobRepository.save(job); // Cập nhật tiến độ

                pageable = pageable.next();
            } while (page.hasNext());

            job.setStatus(JobStatus.COMPLETED);
        } catch (Exception e) {
            job.setStatus(JobStatus.FAILED);
            job.setErrorMessage(e.getMessage());
        } finally {
            job.setEndTime(LocalDateTime.now());
            jobRepository.save(job);
        }
    }

    @Async
    public void reIndexAddresses(String jobId) {
        // 1. Xóa và khởi tạo lại Index
        IndexOperations indexOps = esOperations.indexOps(AddressDocument.class);
        indexOps.delete();
        indexOps.create();

        BatchJob job = jobRepository.findById(jobId).orElseThrow();
        job.setStatus(JobStatus.RUNNING);
        job.setStartTime(LocalDateTime.now());
        jobRepository.save(job);

        try {
            int pageSize = 100;
            Pageable pageable = PageRequest.of(0, pageSize);
            Page<Address> page;
            int processed = 0;

            long total = addressRepository.count();
            job.setTotalItems((int) total);

            do {
                page = addressRepository.findAll(pageable);
                addressSearchService.bulkIndexAddresses(page.getContent());

                processed += page.getContent().size();
                job.setProcessedItems(processed);
                jobRepository.save(job);

                pageable = pageable.next();
            } while (page.hasNext());

            job.setStatus(JobStatus.COMPLETED);
        } catch (Exception e) {
            job.setStatus(JobStatus.FAILED);
            job.setErrorMessage(e.getMessage());
        } finally {
            job.setEndTime(LocalDateTime.now());
            jobRepository.save(job);
        }
    }
}