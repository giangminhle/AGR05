import { useEffect, useState } from 'react';
import { Alert, Button, Card, Col, Form, Row, Table } from 'react-bootstrap';
import { danhMucApi } from '../api/index.js';
import TieuDeTrang from '../components/TieuDeTrang.jsx';
import { useXacNhan } from '../components/XacNhan.jsx';
import { usePhanTrang } from '../components/PhanTrang.jsx';
import ONhap from '../components/ONhap.jsx';
import { useThongBaoNho } from '../components/ThongBaoNho.jsx';

const RONG = { maLoai: null, tenLoai: '', trieuChung: '', bienPhapPhongTru: '' };

/** UC13 Danh mục sâu bệnh: bảng bên trái, ô nhập và nút bên phải (bố cục như KhuVucPage). */
export default function DanhMucSauBenhPage() {
  const hienToast = useThongBaoNho();
  const xacNhan = useXacNhan();
  const [ds, setDs] = useState([]);
  const [tim, setTim] = useState('');
  const [form, setForm] = useState(RONG);
  const [loi, setLoi] = useState(null);

  const tai = () => danhMucApi.layLoai().then(setDs).catch(setLoi);
  useEffect(() => { tai(); }, []);

  const doi = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  async function luu(e) {
    e.preventDefault();
    setLoi(null);
    try {
      if (form.maLoai) await danhMucApi.suaLoai(form.maLoai, form);
      else await danhMucApi.themLoai(form);
      hienToast('Đã lưu loại sâu bệnh.');
      setForm(RONG);
      tai();
    } catch (er) {
      setLoi(er);
    }
  }

  async function xoa() {
    if (!(await xacNhan({ tieuDe: 'Xóa loại sâu bệnh', noiDung: `Xóa loại "${form.tenLoai}"? Thao tác này không hoàn tác được.`, nutDongY: 'Xóa', mau: 'danger' }))) return;
    setLoi(null);
    try {
      await danhMucApi.xoaLoai(form.maLoai);
      hienToast('Đã xóa loại sâu bệnh.');
      setForm(RONG);
      tai();
    } catch (er) {
      setLoi(er);
    }
  }

  const hien = ds.filter((l) => l.tenLoai.toLowerCase().includes(tim.toLowerCase()));
  const { dsTrang, dieuKhien } = usePhanTrang(hien, tim);

  return (
    <>
    <TieuDeTrang tieuDe="Danh mục sâu bệnh" moTa="Thêm, sửa, xóa loại sâu bệnh cùng triệu chứng và biện pháp phòng trừ" />
    <Row>
      <Col lg={7}>
        <Card>
          <Card.Header className="d-flex justify-content-between align-items-center">
            <span className="fw-semibold">Danh sách ({hien.length})</span>
            <Form.Control size="sm" style={{ width: 220 }} placeholder="Tìm theo tên..." value={tim} onChange={(e) => setTim(e.target.value)} />
          </Card.Header>
          <Table hover responsive size="sm" className="bang-chon mb-0">
            <thead><tr><th>#</th><th>Tên loại</th><th>Triệu chứng</th></tr></thead>
            <tbody>
              {dsTrang.map((l) => (
                <tr key={l.maLoai} className={form.maLoai === l.maLoai ? 'dong-chon' : ''} onClick={() => { setForm(l); setLoi(null); }}>
                  <td>{l.maLoai}</td><td>{l.tenLoai}</td><td className="small">{l.trieuChung}</td>
                </tr>
              ))}
            </tbody>
          </Table>
          {dieuKhien}
        </Card>
      </Col>
      <Col lg={5}>
        <Card>
          <Card.Header>{form.maLoai ? `Sửa loại #${form.maLoai}` : 'Thêm loại mới'}</Card.Header>
          <Card.Body>
            {loi && !loi.truong && <Alert variant="danger">{loi.loi}</Alert>}
            <Form onSubmit={luu}>
              <ONhap ten="tenLoai" nhan="Tên loại" batBuoc loi={loi} name="tenLoai" value={form.tenLoai} onChange={doi} maxLength={100} />
              <ONhap ten="trieuChung" nhan="Triệu chứng" loi={loi} as="textarea" rows={3} name="trieuChung" value={form.trieuChung || ''} onChange={doi} />
              <ONhap ten="bienPhapPhongTru" nhan="Biện pháp phòng trừ" loi={loi} as="textarea" rows={3} name="bienPhapPhongTru" value={form.bienPhapPhongTru || ''} onChange={doi} />
              <div className="d-flex justify-content-end gap-2">
                {form.maLoai && <Button variant="outline-danger" onClick={xoa}>Xóa</Button>}
                <Button variant="outline-secondary" onClick={() => { setForm(RONG); setLoi(null); }}>Mới</Button>
                <Button type="submit" variant="success">Lưu</Button>
              </div>
            </Form>
          </Card.Body>
        </Card>
      </Col>
    </Row>
    </>
  );
}
