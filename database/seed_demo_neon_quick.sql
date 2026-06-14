-- Ejecutar en Neon SQL Editor para llenar reportes de inmediato.
-- Requiere que existan empleados (ids 1=PDI, 3=SAG típicamente).

DELETE FROM documentos_adjuntos;
DELETE FROM declaraciones_juradas;
DELETE FROM permisos_menor;
DELETE FROM vehiculos_sat;
DELETE FROM pasajeros;

INSERT INTO pasajeros (documento_enc, tipo_documento, nombres_enc, apellidos_enc, fecha_nacimiento, nacionalidad, menor_edad)
SELECT 'DEMO-A' || g, 'RUT', 'Nombre' || g, 'Apellido' || g, DATE '1990-01-01' + (g || ' days')::interval, 'Chilena', false
FROM generate_series(1, 34) g;

INSERT INTO pasajeros (documento_enc, tipo_documento, nombres_enc, apellidos_enc, fecha_nacimiento, nacionalidad, menor_edad, tutor_id)
SELECT 'DEMO-M' || g, 'RUT', 'Menor' || g, 'Demo' || g, DATE '2016-06-01', 'Chilena', true, 1
FROM generate_series(1, 8) g;

INSERT INTO permisos_menor (pasajero_menor_id, tipo_permiso, numero_documento, fecha_emision, validado, empleado_validador_id)
SELECT p.id, 'NOTARIAL', 'PM-2026-' || p.id, DATE '2026-01-15', true, (SELECT id FROM empleados WHERE rol = 'PDI' LIMIT 1)
FROM pasajeros p WHERE p.menor_edad = true
ORDER BY p.id LIMIT 6;

INSERT INTO vehiculos_sat (patente_enc, pais_origen, tipo_vehiculo, diplomatico, fecha_ingreso, fecha_max_salida, encargo_robo, estado_sat)
SELECT 'PAT-' || g,
       CASE WHEN g % 2 = 0 THEN 'CHILE' ELSE 'ARGENTINA' END,
       CASE WHEN g % 5 = 0 THEN 'CAMIONETA' WHEN g % 7 = 0 THEN 'MOTOCICLETA' ELSE 'AUTOMOVIL' END,
       g = 23,
       CURRENT_DATE - 12,
       CURRENT_DATE + 168,
       g IN (3, 12, 18),
       CASE WHEN g IN (3, 12, 18) THEN 'ALERTA_ROBO' ELSE 'VIGENTE' END
FROM generate_series(1, 24) g;

INSERT INTO declaraciones_juradas (pasajero_id, productos_agro, animales_mascotas, detalle, fecha_declaracion, empleado_sag_id)
SELECT p.id,
       (p.id % 4 = 0),
       (p.id % 5 = 1),
       'Declaración jurada SAG — registro demo Paso Los Libertadores',
       NOW() - ((p.id % 25) || ' days')::interval,
       (SELECT id FROM empleados WHERE rol = 'SAG' LIMIT 1)
FROM pasajeros p
WHERE p.menor_edad = false;

INSERT INTO declaraciones_juradas (pasajero_id, representante_id, productos_agro, animales_mascotas, detalle, fecha_declaracion, empleado_sag_id)
SELECT p.id, p.tutor_id, false, (p.id % 2 = 0),
       'Declaración SAG — menor con representante legal',
       NOW() - INTERVAL '3 days',
       (SELECT id FROM empleados WHERE rol = 'SAG' LIMIT 1)
FROM pasajeros p
WHERE p.menor_edad = true AND p.tutor_id IS NOT NULL
LIMIT 4;

-- Verificar:
SELECT 'pasajeros' AS tabla, COUNT(*) FROM pasajeros
UNION ALL SELECT 'menores', COUNT(*) FROM pasajeros WHERE menor_edad
UNION ALL SELECT 'permisos_validados', COUNT(*) FROM permisos_menor WHERE validado
UNION ALL SELECT 'vehiculos_sat', COUNT(*) FROM vehiculos_sat
UNION ALL SELECT 'alertas_robo', COUNT(*) FROM vehiculos_sat WHERE encargo_robo
UNION ALL SELECT 'declaraciones_sag', COUNT(*) FROM declaraciones_juradas;
