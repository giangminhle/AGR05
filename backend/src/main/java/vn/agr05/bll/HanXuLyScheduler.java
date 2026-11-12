package vn.agr05.bll;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Lịch chạy nền mỗi 60 giây (ARCH04, FR-17). Tắt bằng agr05.lich-nen=false. */
@Component
@ConditionalOnProperty(name = "agr05.lich-nen", havingValue = "true", matchIfMissing = true)
public class HanXuLyScheduler {

    private static final Logger LOG = LoggerFactory.getLogger(HanXuLyScheduler.class);

    private final HanXuLyService hanXuLyService;

    public HanXuLyScheduler(HanXuLyService hanXuLyService) {
        this.hanXuLyService = hanXuLyService;
    }

    @Scheduled(initialDelay = 15_000, fixedDelay = 60_000)
    public void chay() {
        try {
            int quaHan = hanXuLyService.kiemTraQuaHan();
            int dong = hanXuLyService.dongHoSoHetHan();
            if (quaHan + dong > 0) {
                LOG.info("Chức năng nền: gắn cờ quá hạn {} báo cáo, đóng {} hồ sơ", quaHan, dong);
            }
        } catch (RuntimeException e) {
            LOG.error("Chức năng nền lỗi", e);
        }
    }
}
