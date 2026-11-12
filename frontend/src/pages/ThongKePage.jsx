import { useEffect, useState } from 'react';
import { Alert, Button, Card, Col, Form, Row, Spinner, Table } from 'react-bootstrap';
import { Bar, BarChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import { danhMucApi, thongKeApi } from '../api/index.js';
import { TEN_MUC_NGUY_CO, TEN_TRANG_THAI, VaiTro } from '../hangSo.js';
import { homNay, ngayGio, truNgay } from '../tienIch.js';
import { usePhien } from '../components/PhienDangNhap.jsx';
import ONhap from '../components/ONhap.jsx';

/** 6.2.7 Thống kê và cảnh báo (UC11). Chuyên gia chỉ chọn được khu vực mình phụ trách. */
export default function ThongKePage() {
  const nguoiDung = usePhien();
  const [loc, setLoc] = useState({ tu: truNgay(homNay(), 90), den: homNay(), maKhuVuc: '', maLoai: '' });
  const [dsKhuVuc, setDsKhuVuc] = useState([]);
  const [dsLoai, setDsLoai] = useState([]);
  const [bang, setBang] = useState(null);
  const [loi, setLoi] = useState(null);
  const [dangTai, setDangTai] = useState(false);

  useEffect(() => {
    danhMucApi.layKhuVuc().then((kv) =>
      setDsKhuVuc(nguoiDung.vaiTro === VaiTro.CHUYEN_GIA ? kv.filter((k) => nguoiDung.dsMaKhuVuc.includes(k.maKhuVuc)) : kv));
    danhMucApi.layLoai().then(setDsLoai);
  }, [nguoiDung]);

  async function xem(e) {
    e?.preventDefault();
    setLoi(null);
    setDangTai(true);
    try {
      setBang(await thongKeApi.thongKe(loc));
    } catch (er) {
      setLoi(er);
    } finally {
      setDangTai(false);
    }
  }

  // Lần đầu mở trang thì xem luôn kỳ mặc định
  // eslint-disable-next-line react-hooks/exhaustive-deps
  useEffect(() => { xem(); }, []);

  const doi = (e) => setLoc({ ...loc, [e.target.name]: e.target.value });
  const duLieuTuan = bang ? Object.entries(bang.theoTuan).map(([tuan, soCa]) => ({ tuan, soCa })) : [];

  return (
    <>
      <Card className="mb-3">
        <Card.Body>
          <Form onSubmit={xem}>
            <Row className="align-items-end">
              <Col md={2}><ONhap ten="tuNgay" nhan="Từ ngày" loi={loi} type="date" name="tu" value={loc.tu} onChange={doi} /></Col>
              <Col md={2}><ONhap ten="denNgay" nhan="Đến ngày" loi={loi} type="date" name="den" value={loc.den} onChange={doi} /></Col>
              <Col md={3}>
                <Form.Group className="mb-3">
                  <Form.Label>Khu vực</Form.Label>
                  <Form.Select name="maKhuVuc" value={loc.maKhuVuc} onChange={doi}>
                    <option value="">{nguoiDung.vaiTro === VaiTro.CHUYEN_GIA ? 'Mọi khu vực phụ trách' : 'Mọi khu vực'}</option>
                    {dsKhuVuc.map((k) => <option key={k.maKhuVuc} value={k.maKhuVuc}>{k.tenKhuVuc}</option>)}
                  </Form.Select>
                </Form.Group>
              </Col>
              <Col md={3}>
                <Form.Group className="mb-3">
                  <Form.Label>Loại sâu bệnh</Form.Label>
                  <Form.Select name="maLoai" value={loc.maLoai} onChange={doi}>
                    <option value="">Mọi loại</option>
                    {dsLoai.map((l) => <option key={l.maLoai} value={l.maLoai}>{l.tenLoai}</option>)}
                  </Form.Select>
                </Form.Group>
              </Col>
              <Col md={2} className="mb-3 d-flex justify-content-end">
                <Button type="submit" variant="success" disabled={dangTai}>{dangTai && <Spinner size="sm" className="me-2" />}Xem</Button>
              </Col>
            </Row>
          </Form>
          {loi && !['tuNgay', 'denNgay'].includes(loi.truong) && <Alert variant="danger" className="mb-0">{loi.loi}</Alert>}
        </Card.Body>
      </Card>

      {bang && (
        <Row>
          <Col lg={4}>
            <Card className="mb-3">
              <Card.Header as="h6">Số báo cáo theo trạng thái</Card.Header>
              <Table size="sm" className="mb-0">
                <tbody>
                  {Object.entries(bang.theoTrangThai).map(([tt, so]) => (
                    <tr key={tt}><td>{TEN_TRANG_THAI[tt]}</td><td className="text-end">{so}</td></tr>
                  ))}
                  <tr className="fw-bold"><td>Tổng</td><td className="text-end">{Object.values(bang.theoTrangThai).reduce((a, b) => a + b, 0)}</td></tr>
                </tbody>
              </Table>
            </Card>
            <Card className="mb-3">
              <Card.Header as="h6">Dự báo AI theo mức</Card.Header>
              <Table size="sm" className="mb-0">
                <tbody>
                  {Object.entries(bang.duBaoTheoMuc).map(([m, so]) => (
                    <tr key={m}><td>{TEN_MUC_NGUY_CO[m]}</td><td className="text-end">{so}</td></tr>
                  ))}
                </tbody>
              </Table>
            </Card>
          </Col>
          <Col lg={8}>
            <Card className="mb-3">
              <Card.Header as="h6">Số ca đã xác minh theo tuần</Card.Header>
              <Card.Body style={{ height: 280 }}>
                {duLieuTuan.length === 0 ? <p className="text-muted">Không có ca nào trong kỳ.</p> : (
                  <ResponsiveContainer width="100%" height="100%">
                    <BarChart data={duLieuTuan}>
                      <CartesianGrid strokeDasharray="3 3" />
                      <XAxis dataKey="tuan" fontSize={11} />
                      <YAxis allowDecimals={false} />
                      <Tooltip formatter={(v) => [v, 'Số ca']} labelFormatter={(l) => 'Tuần bắt đầu ' + l} />
                      <Bar dataKey="soCa" fill="#198754" />
                    </BarChart>
                  </ResponsiveContainer>
                )}
              </Card.Body>
            </Card>
          </Col>
          <Col lg={12}>
            <Card>
              <Card.Header as="h6">Cảnh báo trong kỳ</Card.Header>
              <Card.Body className="p-0">
                {bang.dsCanhBao.length === 0 ? <p className="text-muted p-3 mb-0">Không có cảnh báo.</p> : (
                  <Table size="sm" className="mb-0">
                    <thead>
                      <tr><th>Ngày tạo</th><th>Khu vực</th><th>Loại</th><th>Nguồn</th><th>Nội dung</th></tr>
                    </thead>
                    <tbody>
                      {bang.dsCanhBao.map((c) => (
                        <tr key={c.maCanhBao}>
                          <td>{ngayGio(c.ngayTao)}</td>
                          <td>{c.tenKhuVuc}</td>
                          <td>{c.tenLoai}</td>
                          <td>{c.maDuBao ? 'AI (BR08)' : 'Ngưỡng (BR06)'}</td>
                          <td>{c.noiDung}</td>
                        </tr>
                      ))}
                    </tbody>
                  </Table>
                )}
              </Card.Body>
            </Card>
          </Col>
        </Row>
      )}
    </>
  );
}
