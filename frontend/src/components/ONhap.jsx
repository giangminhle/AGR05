import { Form } from 'react-bootstrap';

/**
 * Ô nhập có nhãn phía trên, dấu (*) khi bắt buộc, viền đỏ và dòng lỗi khi API báo lỗi đúng ô này (luật 15).
 * loi = { loi, truong } lấy từ LoiApi.
 */
export default function ONhap({ ten, nhan, batBuoc, loi, children, as, ...props }) {
  const sai = loi && loi.truong === ten;
  return (
    <Form.Group className="mb-3" controlId={'o-' + ten}>
      <Form.Label>
        {nhan} {batBuoc && <span className="text-danger">*</span>}
      </Form.Label>
      {children ? (
        children(sai)
      ) : (
        <Form.Control as={as} name={ten} isInvalid={sai} {...props} />
      )}
      {sai && <Form.Control.Feedback type="invalid" className="d-block">{loi.loi}</Form.Control.Feedback>}
    </Form.Group>
  );
}
