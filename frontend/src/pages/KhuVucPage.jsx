import { useEffect, useState } from 'react';
import { Alert, Button, Card, Col, Form, Row, Table } from 'react-bootstrap';
import { danhMucApi, nguoiDungApi } from '../api/index.js';
import { VaiTro } from '../hangSo.js';
import ONhap from '../components/ONhap.jsx';
import { useThongBaoNho } from '../components/ThongBaoNho.jsx';

const RONG = { maKhuVuc: null, tenKhuVuc: '', moTa: '' };

/** 6.2.8 Khu vực và phân công (UC14). */
export default function KhuVucPage() {
  const hienToast = useThongBaoNho();
  const [ds, setDs] = useState([]);
  const [form, setForm] = useState(RONG);
  const [dsChuyenGia, setDsChuyenGia] = useState([]);
  const [tatCaChuyenGia, setTatCaChuyenGia] = useState([]);
  const [chonChuyenGia, setChonChuyenGia] = useState('');
  const [loi, setLoi] = useState(null);

  const tai = () => danhMucApi.layKhuVuc().then(setDs).catch(setLoi);
  useEffect(() => {
    tai();
    nguoiDungApi.layTatCa().then((nd) => setTatCaChuyenGia(nd.filter((n) => n.vaiTro === VaiTro.CHUYEN_GIA && !n.biKhoa)));
  }, []);

  const taiChuyenGia = (ma) => danhMucApi.layChuyenGia(ma).then(setDsChuyenGia);

  function chonKhuVuc(kv) {
    setForm(kv);
    setLoi(null);
    taiChuyenGia(kv.maKhuVuc);
  }

  async function luu(e) {
    e.preventDefault();
    setLoi(null);
    try {
      if (form.maKhuVuc) await danhMucApi.suaKhuVuc(form.maKhuVuc, form);
      else await danhMucApi.themKhuVuc(form);
      hienToast('Đã lưu khu vực.');
      tai();
    } catch (er) {
      setLoi(er);
    }
  }

  async function nutPhanCong() {
    setLoi(null);
    if (!chonChuyenGia) {
      setLoi({ loi: 'Chọn một chuyên gia.', truong: 'chonChuyenGia' });
      return;
    }
    try {
      await danhMucApi.phanCong(form.maKhuVuc, Number(chonChuyenGia));
      hienToast('Đã phân công.');
      setChonChuyenGia('');
      taiChuyenGia(form.maKhuVuc);
      tai();
    } catch (er) {
      setLoi(er);
    }
  }

  async function nutGoPhanCong(cg) {
    if (!window.confirm(`Gỡ phân công ${cg.hoTen} khỏi khu vực này?`)) return;
    try {
      const kq = await danhMucApi.goPhanCong(form.maKhuVuc, cg.maNguoiDung);
      hienToast(kq.conChuyenGia ? 'Đã gỡ phân công.' : 'Đã gỡ. Khu vực không còn chuyên gia, hãy phân công người khác.',
        kq.conChuyenGia ? 'success' : 'warning');
      taiChuyenGia(form.maKhuVuc);
      tai();
    } catch (er) {
      setLoi(er);
    }
  }

  const doi = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  return (
    <Row>
      <Col lg={6}>
        <Card>
          <Card.Header as="h5">Khu vực</Card.Header>
          <Table hover size="sm" className="bang-chon mb-0">
            <thead><tr><th>#</th><th>Tên khu vực</th><th>Mô tả</th><th className="text-end">Số chuyên gia</th></tr></thead>
            <tbody>
              {ds.map((k) => (
                <tr key={k.maKhuVuc} className={form.maKhuVuc === k.maKhuVuc ? 'dong-chon' : ''} onClick={() => chonKhuVuc(k)}>
                  <td>{k.maKhuVuc}</td><td>{k.tenKhuVuc}</td><td>{k.moTa}</td>
                  <td className={'text-end' + (k.soChuyenGia === 0 ? ' text-danger fw-bold' : '')}>{k.soChuyenGia}</td>
                </tr>
              ))}
            </tbody>
          </Table>
        </Card>
      </Col>
      <Col lg={6}>
        <Card className="mb-3">
          <Card.Header as="h6">{form.maKhuVuc ? `Sửa khu vực #${form.maKhuVuc}` : 'Thêm khu vực'}</Card.Header>
          <Card.Body>
            {loi && !loi.truong && <Alert variant="danger">{loi.loi}</Alert>}
            <Form onSubmit={luu}>
              <ONhap ten="tenKhuVuc" nhan="Tên khu vực" batBuoc loi={loi} name="tenKhuVuc" value={form.tenKhuVuc} onChange={doi} maxLength={100} />
              <ONhap ten="moTa" nhan="Mô tả" loi={loi} name="moTa" value={form.moTa || ''} onChange={doi} maxLength={255} />
              <div className="d-flex justify-content-end gap-2">
                <Button variant="outline-secondary" onClick={() => { setForm(RONG); setDsChuyenGia([]); setLoi(null); }}>Mới</Button>
                <Button type="submit" variant="success">Lưu</Button>
              </div>
            </Form>
          </Card.Body>
        </Card>
        {form.maKhuVuc && (
          <Card>
            <Card.Header as="h6">Chuyên gia phụ trách {form.tenKhuVuc}</Card.Header>
            <Card.Body>
              <Table size="sm">
                <tbody>
                  {dsChuyenGia.length === 0 && <tr><td className="text-danger">Chưa có chuyên gia phụ trách.</td></tr>}
                  {dsChuyenGia.map((cg) => (
                    <tr key={cg.maNguoiDung}>
                      <td>{cg.hoTen} ({cg.tenDangNhap}){cg.biKhoa && <span className="text-danger"> – đã khóa</span>}</td>
                      <td className="text-end"><Button size="sm" variant="outline-danger" onClick={() => nutGoPhanCong(cg)}>Gỡ phân công</Button></td>
                    </tr>
                  ))}
                </tbody>
              </Table>
              <ONhap ten="chonChuyenGia" nhan="Giao thêm chuyên gia" loi={loi}>
                {(sai) => (
                  <div className="d-flex gap-2">
                    <Form.Select value={chonChuyenGia} onChange={(e) => setChonChuyenGia(e.target.value)} isInvalid={sai}>
                      <option value="">-- Chọn chuyên gia --</option>
                      {tatCaChuyenGia.map((n) => <option key={n.maNguoiDung} value={n.maNguoiDung}>{n.hoTen} ({n.tenDangNhap})</option>)}
                    </Form.Select>
                    <Button onClick={nutPhanCong}>Phân công</Button>
                  </div>
                )}
              </ONhap>
            </Card.Body>
          </Card>
        )}
      </Col>
    </Row>
  );
}
