-- Datos demo para reportes EFT — Paso Los Libertadores
-- Ejecutar en Neon SQL Editor SI ya tiene empleados pero reportes en 0.
-- Nota: los campos cifrados usan valores ficticios; para cifrado real use el
--       DemoDataInitializer del backend (se carga solo si pasajeros = 0).

-- Opción A (recomendada): borrar datos operativos y reiniciar Render para que
-- DemoDataInitializer cargue todo automáticamente con cifrado AES correcto:
--
--   DELETE FROM documentos_adjuntos;
--   DELETE FROM declaraciones_juradas;
--   DELETE FROM permisos_menor;
--   DELETE FROM vehiculos_sat;
--   DELETE FROM pasajeros;
--   -- Luego reinicie el servicio en Render (Manual Deploy)

-- Opción B: insertar conteos mínimos sin cifrado (solo para ver números en PDF)
-- Descomente el bloque siguiente si necesita cifras YA sin redeploy:

/*
INSERT INTO pasajeros (documento_enc, tipo_documento, nombres_enc, apellidos_enc, fecha_nacimiento, nacionalidad, menor_edad)
SELECT 'DEMO' || g, 'RUT', 'Nombre' || g, 'Apellido' || g, '1990-01-01'::date, 'Chilena', false
FROM generate_series(1, 44) g;

INSERT INTO pasajeros (documento_enc, tipo_documento, nombres_enc, apellidos_enc, fecha_nacimiento, nacionalidad, menor_edad, tutor_id)
SELECT 'MENOR' || g, 'RUT', 'Menor' || g, 'Demo' || g, '2016-06-01'::date, 'Chilena', true, 1
FROM generate_series(1, 12) g;

INSERT INTO permisos_menor (pasajero_menor_id, tipo_permiso, numero_documento, fecha_emision, validado, empleado_validador_id)
SELECT p.id, 'NOTARIAL', 'PM-DEMO-' || p.id, '2026-01-15', true, 1
FROM pasajeros p WHERE p.menor_edad = true LIMIT 8;

INSERT INTO vehiculos_sat (patente_enc, pais_origen, tipo_vehiculo, diplomatico, fecha_ingreso, fecha_max_salida, encargo_robo, estado_sat)
SELECT 'PAT' || g, CASE WHEN g % 2 = 0 THEN 'CHILE' ELSE 'ARGENTINA' END, 'AUTOMOVIL', false,
       CURRENT_DATE - 10, CURRENT_DATE + 170, g IN (3, 7, 15), 'VIGENTE'
FROM generate_series(1, 24) g;

INSERT INTO declaraciones_juradas (pasajero_id, productos_agro, animales_mascotas, detalle, fecha_declaracion, empleado_sag_id)
SELECT p.id, false, false, 'Declaración demo SAG', NOW() - (p.id || ' days')::interval, 3
FROM pasajeros p WHERE p.menor_edad = false LIMIT 36;
*/

-- Cifras esperadas tras DemoDataInitializer (Opción A):
--   Pasajeros registrados .............. 42
--   Pasajeros menores ..................  8
--   Permisos de menores validados ......  6
--   Vehículos SAT ...................... 24
--   Alertas por encargo de robo ........  3
--   Declaraciones juradas SAG .......... 36
