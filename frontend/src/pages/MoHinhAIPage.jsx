import { useEffect, useState } from 'react';
import { Alert, Badge, Button, Card, Col, Row, Spinner, Table } from 'react-bootstrap';
import { moHinhApi } from '../api/index.js';
import { ngayGio } from '../tienIch.js';
import { useThongBaoNho } from '../components/ThongBaoNho.jsx';

/** 6.2.10 Mô hình AI (UC19): bản đang dùng, huấn luyện lại, bảng phiên bản, kích hoạt. */
export default function MoHinhAIPage() {
  const hienToast = useThongBaoNho();
  const [du, setDu] = useState(null);
  const [chon, setChon] = useState(null);
  const [tienTrinh, setTienTrinh] = useState('');
  const [dangHuanLuyen, setDangHuanLuyen] = useState(false);
  const [loi, setLoi] = useState('');

  const tai = () => moHinhApi.layDanhSach().then(setDu).catch((e) => setLoi(e.loi));
  useEffect(() => { tai(); }, []);

  async function nutHuanLuyen() {
    setLoi('');
    setDangHuanLuyen(true);
    setTienTrinh('Đang huấn luyện trên dữ liệu đã xác minh...');
    try {
      const mh = await moHinhApi.huanLuyen();
      setTienTrinh(`Huấn luyện xong phiên bản ${mh.phienBan} (chưa kích hoạt).`);
      setChon(mh.maMoHinh);
      tai();
    } catch (e) {
      setTienTrinh('');
      setLoi(e.loi);
    } finally {
      setDangHuanLuyen(false);
    }
  }

  async function nutKichHoat() {
    setLoi('');
    try {
      await moHinhApi.kichHoat(chon);
      hienToast('Đã kích hoạt phiên bản mới.');
      tai();
    } catch (e) {
      setLoi(e.loi);
    }
  }

  if (!du) return loi ? <Alert variant="danger">{loi}</Alert> : <Spinner animation="border" />;
  const dd = du.dangSuDung;
  const phanTram = (x) => (x * 100).toFixed(1) + '%';

  return (
    <Row>
      <Col lg={4}>
        <Card className="mb-3">
          <Card.Header as="h6">Phiên bản đang dùng</Card.Header>
          <Card.Body>
            {dd ? (
              <>
                <h5>{dd.phienBan}</h5>
                <p className="mb-1">Huấn luyện: {ngayGio(dd.ngayHuanLuyen)}</p>
                <p className="mb-1">Số mẫu huấn luyện: {dd.soMauHuanLuyen}</p>
                <p className="mb-0">Accuracy {phanTram(dd.accuracy)} – F1 {phanTram(dd.f1)} – AUC {phanTram(dd.auc)}</p>
              </>
            ) : (
              <p className="text-muted mb-0">Chưa có mô hình đang dùng. Hệ thống chỉ cảnh báo theo ngưỡng BR06.</p>
            )}
          </Card.Body>
        </Card>
        <Card>
          <Card.Body>
            <Button variant="success" className="w-100 mb-2" onClick={nutHuanLuyen} disabled={dangHuanLuyen}>
              {dangHuanLuyen && <Spinner size="sm" className="me-2" />}Huấn luyện lại
            </Button>
            <div className="small text-muted">{tienTrinh}</div>
          </Card.Body>
        </Card>
      </Col>
      <Col lg={8}>
        {loi && <Alert variant="danger">{loi}</Alert>}
        <Card>
          <Card.Header as="h6">Các phiên bản</Card.Header>
          <Card.Body className="p-0">
            {du.dsPhienBan.length === 0 ? <p className="text-muted p-3 mb-0">Chưa có phiên bản nào.</p> : (
              <Table hover size="sm" className="bang-chon mb-0">
                <thead>
                  <tr><th>Phiên bản</th><th>Ngày huấn luyện</th><th>Số mẫu</th><th>Accuracy</th><th>F1</th><th>AUC</th><th /></tr>
                </thead>
                <tbody>
                  {du.dsPhienBan.map((m) => (
                    <tr key={m.maMoHinh} className={chon === m.maMoHinh ? 'dong-chon' : ''} onClick={() => setChon(m.maMoHinh)}>
                      <td>{m.phienBan}</td>
                      <td>{ngayGio(m.ngayHuanLuyen)}</td>
                      <td>{m.soMauHuanLuyen}</td>
                      <td>{phanTram(m.accuracy)}</td>
                      <td>{phanTram(m.f1)}</td>
                      <td>{phanTram(m.auc)}</td>
                      <td>{m.dangSuDung && <Badge bg="success">Đang dùng</Badge>}</td>
                    </tr>
                  ))}
                </tbody>
              </Table>
            )}
          </Card.Body>
          <Card.Footer className="d-flex justify-content-end">
            <Button onClick={nutKichHoat} disabled={!chon || du.dsPhienBan.find((m) => m.maMoHinh === chon)?.dangSuDung}>
              Kích hoạt bản đang chọn
            </Button>
          </Card.Footer>
        </Card>
      </Col>
    </Row>
  );
}
