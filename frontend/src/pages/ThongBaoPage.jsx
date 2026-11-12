import { useEffect, useState } from 'react';
import { Alert, Button, Card, Col, Row, Spinner, Table } from 'react-bootstrap';
import { useNavigate } from 'react-router-dom';
import { thongBaoApi } from '../api/index.js';
import { TEN_LOAI_THONG_BAO, VaiTro } from '../hangSo.js';
import { ngayGio } from '../tienIch.js';
import { usePhien } from '../components/PhienDangNhap.jsx';

/** 6.2.6 Thông báo (UC12, UC17). Click một dòng thì đánh dấu đã đọc (BR07). */
export default function ThongBaoPage() {
  const nguoiDung = usePhien();
  const dieuHuong = useNavigate();
  const [ds, setDs] = useState(null);
  const [chon, setChon] = useState(null);
  const [loi, setLoi] = useState('');

  useEffect(() => {
    thongBaoApi.layThongBao().then(setDs).catch((e) => setLoi(e.loi));
  }, []);

  async function moThongBao(tb) {
    setChon(tb);
    if (!tb.daDoc) {
      try {
        await thongBaoApi.danhDauDaDoc(tb.maThongBao);
        setDs((cu) => cu.map((t) => (t.maThongBao === tb.maThongBao ? { ...t, daDoc: true } : t)));
        window.dispatchEvent(new Event('agr05:thong-bao-thay-doi'));
      } catch (e) {
        setLoi(e.loi);
      }
    }
  }

  function nutMoBaoCao() {
    if (nguoiDung.vaiTro === VaiTro.NONG_DAN) dieuHuong(`/bao-cao/cua-toi?ma=${chon.maBaoCao}`);
    else if (nguoiDung.vaiTro === VaiTro.CHUYEN_GIA) dieuHuong(`/xac-minh?ma=${chon.maBaoCao}`);
  }

  if (!ds) return loi ? <Alert variant="danger">{loi}</Alert> : <Spinner animation="border" />;

  return (
    <Row>
      <Col lg={7}>
        <Card>
          <Card.Header as="h5">Thông báo</Card.Header>
          <Card.Body className="p-0">
            {loi && <Alert variant="danger" className="m-2">{loi}</Alert>}
            {ds.length === 0 ? (
              <p className="text-muted p-3 mb-0">Chưa có thông báo.</p>
            ) : (
              <Table hover size="sm" className="bang-chon mb-0">
                <thead>
                  <tr>
                    <th>Ngày</th>
                    <th>Loại</th>
                    <th>Tiêu đề</th>
                  </tr>
                </thead>
                <tbody>
                  {ds.map((t) => (
                    <tr key={t.maThongBao} onClick={() => moThongBao(t)}
                      className={(t.daDoc ? '' : 'dong-chua-doc ') + (chon?.maThongBao === t.maThongBao ? 'dong-chon' : '')}>
                      <td>{ngayGio(t.ngayTao)}</td>
                      <td>{TEN_LOAI_THONG_BAO[t.loai] || t.loai}</td>
                      <td>{t.tieuDe}</td>
                    </tr>
                  ))}
                </tbody>
              </Table>
            )}
          </Card.Body>
        </Card>
      </Col>
      <Col lg={5}>
        <Card>
          <Card.Header as="h6">Nội dung</Card.Header>
          <Card.Body>
            {!chon ? (
              <p className="text-muted">Chọn một thông báo để xem.</p>
            ) : (
              <>
                <h6>{chon.tieuDe}</h6>
                <p style={{ whiteSpace: 'pre-wrap' }}>{chon.noiDung}</p>
                {chon.maBaoCao && nguoiDung.vaiTro !== VaiTro.QUAN_TRI && (
                  <div className="d-flex justify-content-end">
                    <Button size="sm" onClick={nutMoBaoCao}>Mở báo cáo #{chon.maBaoCao}</Button>
                  </div>
                )}
              </>
            )}
          </Card.Body>
        </Card>
      </Col>
    </Row>
  );
}
