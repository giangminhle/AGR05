import { createContext, useCallback, useContext, useRef, useState } from 'react';
import { Button, Modal } from 'react-bootstrap';

const Ctx = createContext(() => Promise.resolve(false));

/** Hộp xác nhận dùng chung (thay window.confirm). const ok = await xacNhan({ noiDung, nutDongY, mau }). */
export function XacNhanProvider({ children }) {
  const [hop, setHop] = useState(null);
  const traLoi = useRef(null);

  const xacNhan = useCallback((tuyChon) => new Promise((resolve) => {
    traLoi.current = resolve;
    setHop({ tieuDe: 'Xác nhận', nutDongY: 'Đồng ý', mau: 'primary', ...tuyChon });
  }), []);

  function dong(ketQua) {
    traLoi.current?.(ketQua);
    traLoi.current = null;
    setHop(null);
  }

  return (
    <Ctx.Provider value={xacNhan}>
      {children}
      <Modal show={Boolean(hop)} onHide={() => dong(false)} centered>
        <Modal.Header closeButton>
          <Modal.Title as="h5">{hop?.tieuDe}</Modal.Title>
        </Modal.Header>
        <Modal.Body>{hop?.noiDung}</Modal.Body>
        <Modal.Footer>
          <Button variant="outline-secondary" onClick={() => dong(false)}>Hủy</Button>
          <Button variant={hop?.mau} onClick={() => dong(true)} autoFocus>{hop?.nutDongY}</Button>
        </Modal.Footer>
      </Modal>
    </Ctx.Provider>
  );
}

export function useXacNhan() {
  return useContext(Ctx);
}
