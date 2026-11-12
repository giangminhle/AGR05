import { createContext, useCallback, useContext, useState } from 'react';
import { Toast, ToastContainer } from 'react-bootstrap';

const Ctx = createContext(() => {});

/** Thông báo nhỏ (toast) khi thao tác thành công. */
export function ThongBaoNhoProvider({ children }) {
  const [ds, setDs] = useState([]);
  const hien = useCallback((noiDung, mau = 'success') => {
    const id = Date.now() + Math.random();
    setDs((cu) => [...cu, { id, noiDung, mau }]);
    setTimeout(() => setDs((cu) => cu.filter((t) => t.id !== id)), 3500);
  }, []);
  return (
    <Ctx.Provider value={hien}>
      {children}
      <ToastContainer position="bottom-end" className="p-3" style={{ zIndex: 2000 }}>
        {ds.map((t) => (
          <Toast key={t.id} bg={t.mau} onClose={() => setDs((cu) => cu.filter((x) => x.id !== t.id))}>
            <Toast.Body className="text-white">{t.noiDung}</Toast.Body>
          </Toast>
        ))}
      </ToastContainer>
    </Ctx.Provider>
  );
}

export function useThongBaoNho() {
  return useContext(Ctx);
}
