# 🌐 Comunicación Frontend-Backend con JWT

## 📋 Índice
1. [Configuración CORS](#configuración-cors)
2. [Flujo de Autenticación](#flujo-de-autenticación)
3. [Ejemplos de Código Frontend](#ejemplos-de-código-frontend)
4. [Manejo de Errores](#manejo-de-errores)
5. [Almacenamiento del Token](#almacenamiento-del-token)

---

## 🔧 Configuración CORS

### Backend (Spring Boot)

**Archivo:** `*/config/SecurityConfig.java`

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    
    // Orígenes permitidos (frontend)
    configuration.setAllowedOrigins(Arrays.asList(
        "http://localhost:5173",  // Vite (React/Vue)
        "http://localhost:3000",  // Create React App / Next.js
        "http://127.0.0.1:5173",
        "http://127.0.0.1:3000"
    ));
    
    // Métodos HTTP permitidos
    configuration.setAllowedMethods(Arrays.asList(
        "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
    ));
    
    // Headers permitidos (incluye Authorization para JWT)
    configuration.setAllowedHeaders(Arrays.asList("*"));
    
    // Permitir credenciales (cookies, headers de autorización)
    configuration.setAllowCredentials(true);
    
    // Headers expuestos al frontend
    configuration.setExposedHeaders(Arrays.asList(
        "Authorization", 
        "Content-Type",
        "X-Total-Count"
    ));
    
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}
```

**¿Qué hace?**
- Permite que el frontend en `localhost:5173` o `localhost:3000` se comunique con el backend
- Permite el header `Authorization` para enviar el token JWT
- Permite métodos HTTP necesarios (GET, POST, PUT, DELETE)
- Habilita credenciales para cookies y headers personalizados

---

## 🔄 Flujo de Autenticación Frontend-Backend

### Paso 1: Usuario hace Login/Registro

```
┌──────────┐                    ┌──────────┐
│ Frontend │                    │ Backend  │
│ (React)  │                    │(Usuarios)│
└────┬─────┘                    └────┬─────┘
     │                              │
     │ POST /api/v1/auth/login      │
     │ {email, password}            │
     ├─────────────────────────────>│
     │                              │
     │                              │ Valida credenciales
     │                              │ Genera token JWT
     │                              │
     │ Response:                    │
     │ {                            │
     │   ok: true,                  │
     │   data: {                    │
     │     user: {...},             │
     │     token: "eyJhbGci..."    │
     │   }                          │
     │ }                            │
     │<─────────────────────────────┤
     │                              │
     │ Guarda token en localStorage │
     │                              │
```

### Paso 2: Frontend Envía Token en Requests

```
┌──────────┐                    ┌──────────┐
│ Frontend │                    │ Backend  │
│ (React)  │                    │(Inventario)│
└────┬─────┘                    └────┬─────┘
     │                              │
     │ GET /api/v1/products          │
     │ Authorization: Bearer <token>  │
     ├─────────────────────────────>│
     │                              │
     │                              │ JwtAuthenticationFilter
     │                              │ - Valida token
     │                              │ - Extrae rol
     │                              │
     │                              │ SecurityConfig
     │                              │ - Verifica hasRole("ADMIN")
     │                              │
     │                              │ ProductController
     │                              │ - Retorna productos
     │                              │
     │ Response:                    │
     │ {ok: true, data: [...]}      │
     │<─────────────────────────────┤
     │                              │
```

---

## 💻 Ejemplos de Código Frontend

### 1. Login y Obtención del Token (React)

```javascript
// services/authService.js
const API_URL = 'http://localhost:8081/api/v1/auth';

export const login = async (email, password) => {
  try {
    const response = await fetch(`${API_URL}/login`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ email, password }),
    });

    const data = await response.json();

    if (data.ok && data.data.token) {
      // Guardar token en localStorage
      localStorage.setItem('token', data.data.token);
      localStorage.setItem('user', JSON.stringify(data.data.user));
      return data;
    } else {
      throw new Error(data.message || 'Error en el login');
    }
  } catch (error) {
    console.error('Error en login:', error);
    throw error;
  }
};

export const register = async (userData) => {
  try {
    const response = await fetch(`${API_URL}/register`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(userData),
    });

    const data = await response.json();

    if (data.ok && data.data.token) {
      // Guardar token en localStorage
      localStorage.setItem('token', data.data.token);
      localStorage.setItem('user', JSON.stringify(data.data.user));
      return data;
    } else {
      throw new Error(data.message || 'Error en el registro');
    }
  } catch (error) {
    console.error('Error en registro:', error);
    throw error;
  }
};
```

### 2. Enviar Token en Requests (Axios)

```javascript
// services/api.js
import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8082/api/v1', // Inventario
  headers: {
    'Content-Type': 'application/json',
  },
});

// Interceptor para agregar token automáticamente
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Interceptor para manejar errores 401 (token expirado)
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Token expirado o inválido
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      // Redirigir a login
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default api;
```

### 3. Uso en Componentes React

```javascript
// components/ProductList.jsx
import { useState, useEffect } from 'react';
import api from '../services/api';

const ProductList = () => {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchProducts = async () => {
      try {
        setLoading(true);
        // El token se agrega automáticamente por el interceptor
        const response = await api.get('/products');
        
        if (response.data.ok) {
          setProducts(response.data.data);
        } else {
          setError(response.data.message);
        }
      } catch (err) {
        if (err.response?.status === 403) {
          setError('No tienes permisos para ver todos los productos (requiere ADMIN)');
        } else if (err.response?.status === 401) {
          setError('Sesión expirada. Por favor, inicia sesión nuevamente.');
        } else {
          setError('Error al cargar productos');
        }
      } finally {
        setLoading(false);
      }
    };

    fetchProducts();
  }, []);

  if (loading) return <div>Cargando...</div>;
  if (error) return <div>Error: {error}</div>;

  return (
    <div>
      <h2>Lista de Productos</h2>
      {products.map((product) => (
        <div key={product.id}>
          <h3>{product.name}</h3>
          <p>Precio: ${product.price}</p>
          <p>Stock: {product.stock}</p>
        </div>
      ))}
    </div>
  );
};

export default ProductList;
```

### 4. Crear Producto (Solo ADMIN)

```javascript
// components/CreateProduct.jsx
import { useState } from 'react';
import api from '../services/api';

const CreateProduct = () => {
  const [formData, setFormData] = useState({
    name: '',
    brand: '',
    model: '',
    category: '',
    price: 0,
    stock: 0,
  });
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      setError(null);
      // El token se agrega automáticamente
      const response = await api.post('/products', formData);
      
      if (response.data.ok) {
        setSuccess(true);
        setFormData({ name: '', brand: '', model: '', category: '', price: 0, stock: 0 });
      }
    } catch (err) {
      if (err.response?.status === 403) {
        setError('Solo los administradores pueden crear productos');
      } else if (err.response?.status === 401) {
        setError('Sesión expirada. Por favor, inicia sesión nuevamente.');
      } else {
        setError(err.response?.data?.message || 'Error al crear producto');
      }
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      {error && <div style={{ color: 'red' }}>{error}</div>}
      {success && <div style={{ color: 'green' }}>Producto creado exitosamente</div>}
      
      <input
        type="text"
        placeholder="Nombre"
        value={formData.name}
        onChange={(e) => setFormData({ ...formData, name: e.target.value })}
        required
      />
      <input
        type="text"
        placeholder="Marca"
        value={formData.brand}
        onChange={(e) => setFormData({ ...formData, brand: e.target.value })}
        required
      />
      <input
        type="number"
        placeholder="Precio"
        value={formData.price}
        onChange={(e) => setFormData({ ...formData, price: parseFloat(e.target.value) })}
        required
      />
      <input
        type="number"
        placeholder="Stock"
        value={formData.stock}
        onChange={(e) => setFormData({ ...formData, stock: parseInt(e.target.value) })}
        required
      />
      
      <button type="submit">Crear Producto</button>
    </form>
  );
};

export default CreateProduct;
```

### 5. Fetch API (Sin Axios)

```javascript
// services/api.js (usando Fetch API nativo)
const API_BASE_URL = 'http://localhost:8082/api/v1';

export const apiRequest = async (endpoint, options = {}) => {
  const token = localStorage.getItem('token');
  
  const headers = {
    'Content-Type': 'application/json',
    ...options.headers,
  };

  // Agregar token si existe
  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  const response = await fetch(`${API_BASE_URL}${endpoint}`, {
    ...options,
    headers,
  });

  const data = await response.json();

  // Manejar errores
  if (!response.ok) {
    if (response.status === 401) {
      // Token expirado
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    throw new Error(data.message || 'Error en la petición');
  }

  return data;
};

// Uso
export const getProducts = () => apiRequest('/products', { method: 'GET' });
export const createProduct = (productData) => 
  apiRequest('/products', { 
    method: 'POST', 
    body: JSON.stringify(productData) 
  });
```

---

## ⚠️ Manejo de Errores

### Códigos de Estado HTTP

| Código | Significado | Acción del Frontend |
|--------|-------------|---------------------|
| **200** | OK | Procesar respuesta normalmente |
| **201** | Created | Mostrar mensaje de éxito |
| **400** | Bad Request | Mostrar error de validación |
| **401** | Unauthorized | Token inválido/expirado → Redirigir a login |
| **403** | Forbidden | Usuario no tiene permisos → Mostrar mensaje |
| **404** | Not Found | Recurso no encontrado |
| **500** | Server Error | Error del servidor → Mostrar mensaje genérico |

### Ejemplo de Manejo de Errores

```javascript
// utils/errorHandler.js
export const handleApiError = (error) => {
  if (error.response) {
    // El servidor respondió con un código de error
    const status = error.response.status;
    const message = error.response.data?.message || 'Error desconocido';

    switch (status) {
      case 401:
        // Token expirado o inválido
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        return {
          type: 'UNAUTHORIZED',
          message: 'Sesión expirada. Por favor, inicia sesión nuevamente.',
          redirect: '/login',
        };

      case 403:
        // Sin permisos
        return {
          type: 'FORBIDDEN',
          message: 'No tienes permisos para realizar esta acción. Se requiere rol ADMIN.',
        };

      case 400:
        // Error de validación
        return {
          type: 'VALIDATION_ERROR',
          message: message,
        };

      case 404:
        return {
          type: 'NOT_FOUND',
          message: 'Recurso no encontrado',
        };

      case 500:
        return {
          type: 'SERVER_ERROR',
          message: 'Error del servidor. Por favor, intenta más tarde.',
        };

      default:
        return {
          type: 'UNKNOWN',
          message: message,
        };
    }
  } else if (error.request) {
    // La petición se hizo pero no hubo respuesta
    return {
      type: 'NETWORK_ERROR',
      message: 'No se pudo conectar con el servidor. Verifica tu conexión.',
    };
  } else {
    // Error al configurar la petición
    return {
      type: 'REQUEST_ERROR',
      message: 'Error al realizar la petición',
    };
  }
};
```

---

## 💾 Almacenamiento del Token

### Opciones de Almacenamiento

#### 1. **localStorage** (Recomendado para este proyecto)

```javascript
// Guardar token
localStorage.setItem('token', token);
localStorage.setItem('user', JSON.stringify(user));

// Obtener token
const token = localStorage.getItem('token');
const user = JSON.parse(localStorage.getItem('user'));

// Eliminar token (logout)
localStorage.removeItem('token');
localStorage.removeItem('user');
```

**Ventajas:**
- ✅ Persiste después de cerrar el navegador
- ✅ Fácil de usar
- ✅ No se envía automáticamente en cada request (más seguro que cookies)

**Desventajas:**
- ⚠️ Vulnerable a XSS si hay código malicioso en la página
- ⚠️ No se elimina automáticamente

#### 2. **sessionStorage** (Alternativa)

```javascript
// Guardar token (solo para la sesión actual)
sessionStorage.setItem('token', token);

// Obtener token
const token = sessionStorage.getItem('token');
```

**Ventajas:**
- ✅ Se elimina automáticamente al cerrar la pestaña
- ✅ Más seguro que localStorage

**Desventajas:**
- ⚠️ No persiste si el usuario cierra y abre el navegador

#### 3. **Cookies HttpOnly** (Más seguro, requiere configuración adicional)

```javascript
// Backend debe configurar cookies HttpOnly
// Frontend no puede acceder directamente a cookies HttpOnly
// Se envían automáticamente en cada request
```

**Ventajas:**
- ✅ Más seguro contra XSS
- ✅ Se envía automáticamente

**Desventajas:**
- ⚠️ Requiere configuración adicional en backend
- ⚠️ Más complejo de implementar

---

## 🔐 Hook Personalizado para Autenticación (React)

```javascript
// hooks/useAuth.js
import { useState, useEffect, createContext, useContext } from 'react';
import { login as loginService, register as registerService } from '../services/authService';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Verificar si hay token guardado al cargar la app
    const savedToken = localStorage.getItem('token');
    const savedUser = localStorage.getItem('user');

    if (savedToken && savedUser) {
      setToken(savedToken);
      setUser(JSON.parse(savedUser));
    }
    setLoading(false);
  }, []);

  const login = async (email, password) => {
    try {
      const response = await loginService(email, password);
      if (response.ok) {
        setToken(response.data.token);
        setUser(response.data.user);
        localStorage.setItem('token', response.data.token);
        localStorage.setItem('user', JSON.stringify(response.data.user));
        return { success: true };
      }
    } catch (error) {
      return { success: false, error: error.message };
    }
  };

  const register = async (userData) => {
    try {
      const response = await registerService(userData);
      if (response.ok) {
        setToken(response.data.token);
        setUser(response.data.user);
        localStorage.setItem('token', response.data.token);
        localStorage.setItem('user', JSON.stringify(response.data.user));
        return { success: true };
      }
    } catch (error) {
      return { success: false, error: error.message };
    }
  };

  const logout = () => {
    setToken(null);
    setUser(null);
    localStorage.removeItem('token');
    localStorage.removeItem('user');
  };

  const isAdmin = () => {
    return user?.role === 'ADMIN';
  };

  const isAuthenticated = () => {
    return !!token && !!user;
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        loading,
        login,
        register,
        logout,
        isAdmin,
        isAuthenticated,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth debe usarse dentro de AuthProvider');
  }
  return context;
};
```

### Uso del Hook

```javascript
// components/ProtectedRoute.jsx
import { useAuth } from '../hooks/useAuth';
import { Navigate } from 'react-router-dom';

const ProtectedRoute = ({ children, requireAdmin = false }) => {
  const { isAuthenticated, isAdmin, loading } = useAuth();

  if (loading) return <div>Cargando...</div>;

  if (!isAuthenticated()) {
    return <Navigate to="/login" />;
  }

  if (requireAdmin && !isAdmin()) {
    return <Navigate to="/unauthorized" />;
  }

  return children;
};

// App.jsx
import { AuthProvider } from './hooks/useAuth';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import ProtectedRoute from './components/ProtectedRoute';
import ProductList from './components/ProductList';
import CreateProduct from './components/CreateProduct';

function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route
            path="/products"
            element={
              <ProtectedRoute requireAdmin={true}>
                <ProductList />
              </ProtectedRoute>
            }
          />
          <Route
            path="/create-product"
            element={
              <ProtectedRoute requireAdmin={true}>
                <CreateProduct />
              </ProtectedRoute>
            }
          />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}
```

---

## 📊 Resumen del Flujo Completo

```
┌─────────────────────────────────────────────────────────────┐
│ 1. USUARIO HACE LOGIN EN FRONTEND                           │
│    - Frontend envía POST /api/v1/auth/login                 │
│    - Backend valida credenciales                             │
│    - Backend genera token JWT con rol                        │
│    - Backend retorna {user, token}                           │
└────────────────────────────┬────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────┐
│ 2. FRONTEND GUARDA TOKEN                                     │
│    - localStorage.setItem('token', token)                   │
│    - localStorage.setItem('user', user)                      │
└────────────────────────────┬────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────┐
│ 3. FRONTEND HACE REQUEST A RECURSO PROTEGIDO                │
│    - Interceptor agrega: Authorization: Bearer <token>      │
│    - Request: GET /api/v1/products                           │
└────────────────────────────┬────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────┐
│ 4. BACKEND VALIDA TOKEN                                      │
│    - JwtAuthenticationFilter extrae token                   │
│    - Valida firma y expiración                               │
│    - Extrae rol: "ADMIN"                                     │
│    - Establece autenticación en SecurityContext              │
└────────────────────────────┬────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────┐
│ 5. BACKEND VERIFICA PERMISOS                                 │
│    - SecurityConfig verifica hasRole("ADMIN")               │
│    - Si es válido → Permite acceso                          │
│    - Si no es válido → 403 Forbidden                         │
└────────────────────────────┬────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────┐
│ 6. BACKEND RETORNA RESPUESTA                                │
│    - Controller ejecuta lógica                              │
│    - Retorna datos: {ok: true, data: [...]}                 │
└────────────────────────────┬────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────┐
│ 7. FRONTEND RECIBE Y PROCESA                                 │
│    - Muestra datos en la UI                                 │
│    - Maneja errores si los hay                              │
└─────────────────────────────────────────────────────────────┘
```

---

## ✅ Checklist de Implementación Frontend

- [ ] Configurar CORS en backend (ya está hecho)
- [ ] Crear servicio de autenticación (login/register)
- [ ] Guardar token en localStorage después de login
- [ ] Configurar interceptor para agregar token automáticamente
- [ ] Manejar errores 401 (token expirado) → Redirigir a login
- [ ] Manejar errores 403 (sin permisos) → Mostrar mensaje
- [ ] Proteger rutas que requieren autenticación
- [ ] Proteger rutas que requieren rol ADMIN
- [ ] Implementar logout (eliminar token)
- [ ] Verificar token al cargar la app

---

## 🎓 Conclusión

La comunicación frontend-backend funciona así:

1. **CORS configurado** permite requests desde `localhost:5173` o `localhost:3000`
2. **Frontend obtiene token** al hacer login/registro
3. **Frontend guarda token** en localStorage
4. **Frontend envía token** en header `Authorization: Bearer <token>` en cada request
5. **Backend valida token** automáticamente con `JwtAuthenticationFilter`
6. **Backend verifica permisos** según el rol del usuario
7. **Frontend maneja respuestas** y errores apropiadamente

¿Necesitas ayuda con alguna parte específica de la implementación frontend? 🚀


