import { useEffect, useState } from 'react';
import { Alert, Button, Card, Form, Table } from 'react-bootstrap';
import { thamSoApi } from '../api/index.js';
import { useThongBaoNho } from '../components/ThongBaoNho.jsx';

/** 6.2.9 Cấu hình hệ thống (UC16): cột Giá trị cho sửa, Lưu gọi PUT cho từng tham số đã sửa. */
export default function CauHinhPage() {
  const hienToast = useThongBaoNho();
  const [ds, setDs] = useState([]);
  const [giaTri, setGiaTri] = useState({});
  const [loi, setLoi] = useState({});
  const [loiChung, setLoiChung] = useState('');

  const tai = () => thamSoApi.layTatCa().then((d) => {
    setDs(d);
    setGiaTri(Object.fromEntries(d.map((t) => [t.maThamSo, String(t.giaTri)])));
  }).catch((e) => setLoiChung(e.loi));
  useEffect(() => { tai(); }, []);

  async function nutLuu() {
    setLoi({});
    setLoiChung('');
    const daSua = ds.filter((t) => String(t.giaTri) !== giaTri[t.maThamSo]);
    if (daSua.length === 0) {
      hienToast('Không có thay đổi.', 'secondary');
      return;
    }
    const loiMoi = {};
    for (const t of daSua) {
      try {
        await thamSoApi.capNhat(t.maThamSo, Number(giaTri[t.maThamSo]));
      } catch (e) {
        loiMoi[t.maThamSo] = e.loi;
      }
    }
    setLoi(loiMoi);
    if (Object.keys(loiMoi).length === 0) hienToast('Đã lưu cấu hình.');
    const conLoi = { ...giaTri };
    await tai();
    // Giữ giá trị sai người dùng đã nhập để họ sửa lại
    setGiaTri((cu) => ({ ...cu, ...Object.fromEntries(Object.keys(loiMoi).map((k) => [k, conLoi[k]])) }));
  }

  return (
    <Card>
      <Card.Header as="h5">Cấu hình hệ thống</Card.Header>
      <Card.Body>
        {loiChung && <Alert variant="danger">{loiChung}</Alert>}
        <Table bordered size="sm">
          <thead><tr><th>Mã tham số</th><th>Mô tả</th><th style={{ width: 200 }}>Giá trị</th></tr></thead>
          <tbody>
            {ds.map((t) => (
              <tr key={t.maThamSo}>
                <td><code>{t.maThamSo}</code></td>
                <td>{t.moTa}</td>
                <td>
                  <Form.Control type="number" size="sm" value={giaTri[t.maThamSo] ?? ''} isInvalid={Boolean(loi[t.maThamSo])}
                    onChange={(e) => setGiaTri({ ...giaTri, [t.maThamSo]: e.target.value })} />
                  {loi[t.maThamSo] && <div className="invalid-feedback d-block">{loi[t.maThamSo]}</div>}
                </td>
              </tr>
            ))}
          </tbody>
        </Table>
        <div className="d-flex justify-content-end">
          <Button variant="success" onClick={nutLuu}>Lưu</Button>
        </div>
      </Card.Body>
    </Card>
  );
}
