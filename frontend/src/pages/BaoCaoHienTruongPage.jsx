import { useEffect, useMemo, useState } from 'react';
import { Alert, Button, Card, CloseButton, Col, Form, Modal, Row, Spinner } from 'react-bootstrap';
import { useNavigate, useParams } from 'react-router-dom';
import { baoCaoApi, danhMucApi, urlAnh } from '../api/index.js';
import { TOI_DA_ANH, TOI_DA_BYTE_ANH, TrangThai } from '../hangSo.js';
import { homNay } from '../tienIch.js';
import ONhap from '../components/ONhap.jsx';
import { useThongBaoNho } from '../components/ThongBaoNho.jsx';

const RONG = { tieuDe: '', tenCayTrong: '', maKhuVuc: '', mucDo: '1', ngayPhatHien: homNay(), viDo: '', kinhDo: '', moTa: '' };

/** 6.2.3 Gửi mới (UC02) và sửa, gửi lại (UC03). Kiểm tra ở đây chỉ để báo sớm; kiểm tra thật ở backend. */
export default function BaoCaoHienTruongPage() {
  const { ma } = useParams();
  const laSua = Boolean(ma);
  const dieuHuong = useNavigate();
  const hienToast = useThongBaoNho();
  const [form, setForm] = useState(RONG);
  const [dsKhuVuc, setDsKhuVuc] = useState([]);
  const [anhCu, setAnhCu] = useState([]);
  const [dsXoa, setDsXoa] = useState([]);
  const [anhMoi, setAnhMoi] = useState([]);
  const [loi, setLoi] = useState(null);
  const [trangThaiCu, setTrangThaiCu] = useState(null);
  const [dangTai, setDangTai] = useState(laSua);
  const [dangGui, setDangGui] = useState(false);
  const [hoiHuy, setHoiHuy] = useState(false);

  useEffect(() => {
    danhMucApi.layKhuVuc().then(setDsKhuVuc).catch((e) => setLoi(e));
  }, []);

  useEffect(() => {
    if (!laSua) {
      setForm(RONG);
      setAnhCu([]);
      setAnhMoi([]);
      setDsXoa([]);
      setTrangThaiCu(null);
      return;
    }
    setDangTai(true);
    baoCaoApi
      .layDeSua(ma)
      .then((bc) => {
        setForm({
          tieuDe: bc.tieuDe, tenCayTrong: bc.tenCayTrong, maKhuVuc: String(bc.maKhuVuc), mucDo: String(bc.mucDo),
          ngayPhatHien: bc.ngayPhatHien, viDo: String(bc.viDo), kinhDo: String(bc.kinhDo), moTa: bc.moTa,
        });
        setAnhCu(bc.dsHinhAnh);
        setTrangThaiCu(bc.trangThai);
      })
      .catch((e) => setLoi(e))
      .finally(() => setDangTai(false));
  }, [ma, laSua]);

  const xemTruoc = useMemo(() => anhMoi.map((f) => ({ f, url: URL.createObjectURL(f) })), [anhMoi]);
  useEffect(() => () => xemTruoc.forEach((x) => URL.revokeObjectURL(x.url)), [xemTruoc]);

  const anhCuConLai = anhCu.filter((h) => !dsXoa.includes(h.maHinhAnh));
  const tongAnh = anhCuConLai.length + anhMoi.length;

  function doi(e) {
    setForm({ ...form, [e.target.name]: e.target.value });
  }

  function nutChonAnh(e) {
    const chon = Array.from(e.target.files || []);
    e.target.value = '';
    for (const f of chon) {
      if (!/\.(jpe?g|png)$/i.test(f.name)) {
        setLoi({ loi: `Ảnh ${f.name} không đúng định dạng, chỉ nhận JPG hoặc PNG.`, truong: 'anh' });
        return;
      }
      if (f.size > TOI_DA_BYTE_ANH) {
        setLoi({ loi: `Ảnh ${f.name} vượt quá 5 MB.`, truong: 'anh' });
        return;
      }
    }
    if (tongAnh + chon.length > TOI_DA_ANH) {
      setLoi({ loi: 'Báo cáo chỉ được có tối đa 5 ảnh.', truong: 'anh' });
      return;
    }
    setLoi(null);
    setAnhMoi([...anhMoi, ...chon]);
  }

  async function nutGui(e) {
    e.preventDefault();
    setLoi(null);
    if (tongAnh < 1) {
      setLoi({ loi: 'Báo cáo phải có ít nhất 1 ảnh.', truong: 'anh' });
      return;
    }
    setDangGui(true);
    try {
      if (laSua) {
        await baoCaoApi.guiLai(ma, form, anhMoi, dsXoa);
        hienToast(trangThaiCu === TrangThai.BI_BAC_BO ? 'Đã gửi lại báo cáo.' : 'Đã lưu thay đổi.');
      } else {
        const kq = await baoCaoApi.guiBaoCao(form, anhMoi);
        hienToast(`Đã gửi báo cáo #${kq.maBaoCao}, đang chờ xác minh.`);
      }
      dieuHuong('/bao-cao/cua-toi');
    } catch (loiApi) {
      setLoi(loiApi); // giữ nguyên dữ liệu đã nhập
    } finally {
      setDangGui(false);
    }
  }

  if (dangTai) return <Spinner animation="border" />;

  const coLoiChung = loi && (!loi.truong || !Object.keys(RONG).includes(loi.truong)) && loi.truong !== 'anh';

  return (
    <Card>
      <Card.Header as="h5">
        {laSua ? (trangThaiCu === TrangThai.BI_BAC_BO ? `Gửi lại báo cáo #${ma}` : `Sửa báo cáo #${ma}`) : 'Gửi báo cáo hiện trường'}
      </Card.Header>
      <Card.Body>
        {coLoiChung && <Alert variant="danger">{loi.loi}</Alert>}
        <Form onSubmit={nutGui} noValidate>
          <Row>
            <Col md={6}>
              <ONhap ten="tieuDe" nhan="Tiêu đề" batBuoc loi={loi} value={form.tieuDe} onChange={doi} maxLength={200} />
            </Col>
            <Col md={6}>
              <ONhap ten="tenCayTrong" nhan="Tên cây trồng" batBuoc loi={loi} value={form.tenCayTrong} onChange={doi} maxLength={100} />
            </Col>
            <Col md={4}>
              <ONhap ten="maKhuVuc" nhan="Khu vực" batBuoc loi={loi}>
                {(sai) => (
                  <Form.Select name="maKhuVuc" value={form.maKhuVuc} onChange={doi} isInvalid={sai}>
                    <option value="">-- Chọn khu vực --</option>
                    {dsKhuVuc.map((k) => <option key={k.maKhuVuc} value={k.maKhuVuc}>{k.tenKhuVuc}</option>)}
                  </Form.Select>
                )}
              </ONhap>
            </Col>
            <Col md={4}>
              <ONhap ten="mucDo" nhan="Mức độ" batBuoc loi={loi}>
                {(sai) => (
                  <Form.Select name="mucDo" value={form.mucDo} onChange={doi} isInvalid={sai}>
                    <option value="1">1 – Nhẹ</option>
                    <option value="2">2 – Trung bình</option>
                    <option value="3">3 – Nặng</option>
                  </Form.Select>
                )}
              </ONhap>
            </Col>
            <Col md={4}>
              <ONhap ten="ngayPhatHien" nhan="Ngày phát hiện" batBuoc loi={loi} type="date" max={homNay()} value={form.ngayPhatHien} onChange={doi} />
            </Col>
            <Col md={6}>
              <ONhap ten="viDo" nhan="Vĩ độ (-90..90)" batBuoc loi={loi} type="number" step="0.000001" min={-90} max={90} value={form.viDo} onChange={doi} />
            </Col>
            <Col md={6}>
              <ONhap ten="kinhDo" nhan="Kinh độ (-180..180)" batBuoc loi={loi} type="number" step="0.000001" min={-180} max={180} value={form.kinhDo} onChange={doi} />
            </Col>
            <Col md={12}>
              <ONhap ten="moTa" nhan="Mô tả hiện trường" batBuoc loi={loi} as="textarea" rows={3} value={form.moTa} onChange={doi} maxLength={1000} />
            </Col>
          </Row>

          <Form.Label>Ảnh hiện trường (1 đến 5 ảnh JPG/PNG, mỗi ảnh tối đa 5 MB) <span className="text-danger">*</span></Form.Label>
          <div className="khung-anh d-flex gap-3 flex-wrap mb-2">
            {anhCuConLai.map((h) => (
              <div key={h.maHinhAnh} className="position-relative">
                <img src={urlAnh(h.maHinhAnh)} alt="Ảnh cũ" />
                <CloseButton className="position-absolute top-0 end-0 bg-white" onClick={() => setDsXoa([...dsXoa, h.maHinhAnh])} />
              </div>
            ))}
            {xemTruoc.map((x, i) => (
              <div key={x.url} className="position-relative">
                <img src={x.url} alt={x.f.name} />
                <CloseButton className="position-absolute top-0 end-0 bg-white" onClick={() => setAnhMoi(anhMoi.filter((_, j) => j !== i))} />
              </div>
            ))}
          </div>
          <Form.Control type="file" accept=".jpg,.jpeg,.png" multiple onChange={nutChonAnh} disabled={tongAnh >= TOI_DA_ANH}
            isInvalid={loi?.truong === 'anh'} className="mb-1" />
          {loi?.truong === 'anh' && <div className="invalid-feedback d-block">{loi.loi}</div>}
          <small className="text-muted">Đã chọn {tongAnh}/{TOI_DA_ANH} ảnh.</small>

          <div className="d-flex justify-content-end gap-2 mt-3">
            <Button variant="outline-secondary" onClick={() => setHoiHuy(true)} disabled={dangGui}>Hủy</Button>
            <Button type="submit" variant="success" disabled={dangGui}>
              {dangGui && <Spinner size="sm" className="me-2" />}
              {laSua ? (trangThaiCu === TrangThai.BI_BAC_BO ? 'Gửi lại' : 'Lưu') : 'Gửi'}
            </Button>
          </div>
        </Form>
      </Card.Body>

      <Modal show={hoiHuy} onHide={() => setHoiHuy(false)}>
        <Modal.Body>Bỏ các thông tin đã nhập và quay lại?</Modal.Body>
        <Modal.Footer>
          <Button variant="secondary" onClick={() => setHoiHuy(false)}>Ở lại</Button>
          <Button variant="danger" onClick={() => dieuHuong('/bao-cao/cua-toi')}>Bỏ và quay lại</Button>
        </Modal.Footer>
      </Modal>
    </Card>
  );
}
