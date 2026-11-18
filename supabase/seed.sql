-- Generado de SeedDataLoader.kt. Ejecutar solo una vez en una base nueva.
begin;
create temporary table carga_inicial (codigo text, nombre text, parte text, tipo text, diseno text, manga text, cuello text, color text, talla text, precio numeric, minimo integer, stock integer, ubicacion1 text, ubicacion2 text) on commit drop;
insert into carga_inicial values
('PAI-JNS-CG-AZU-36-001','Jeans - CG - Azul - 36','Parte Inferior','Jeans','Cargo','','','Azul','36',9990,4,17,'E6-A',null),
('PAI-JNS-CG-AZU-38-001','Jeans - CG - Azul - 38','Parte Inferior','Jeans','Cargo','','','Azul','38',9990,4,17,'E6-A',null),
('PAI-JNS-CG-CEL-40-001','Jeans - CG - Celeste - 40','Parte Inferior','Jeans','Cargo','','','Celeste','40',9990,4,17,'E6-B',null),
('PAI-JNS-CG-NEG-42-001','Jeans - CG - Negro - 42','Parte Inferior','Jeans','Cargo','','','Negro','42',9990,4,16,'E6-B','ZA1'),
('PAI-JNS-CG-NEG-44-001','Jeans - CG - Negro - 44','Parte Inferior','Jeans','Cargo','','','Negro','44',9990,4,16,'E6-B','ZA1'),
('PAI-SHO-DEN-AZU-36-001','Short - DEN - Azul - 36','Parte Inferior','Short','Denim','','','Azul','36',9990,4,17,'E7-A',null),
('PAI-SHO-DEN-CEL-38-001','Short - DEN - Celeste - 38','Parte Inferior','Short','Denim','','','Celeste','38',9990,4,17,'E7-B',null),
('PAI-SHO-DEN-NEG-40-001','Short - DEN - Negro - 40','Parte Inferior','Short','Denim','','','Negro','40',9990,4,17,'E7-B',null),
('PAI-SHO-DEN-AZU-42-001','Short - DEN - Azul - 42','Parte Inferior','Short','Denim','','','Azul','42',9990,4,17,'E7-A','ZA2'),
('PAI-SHO-DEN-CEL-44-001','Short - DEN - Celeste - 44','Parte Inferior','Short','Denim','','','Celeste','44',9990,4,17,'ZA2',null),
('PAS-POL-CCR-NEG-EST-001','Poleron - CCR - Negro - EST','Parte Superior','Poleron','Con Cierre','','','Negro','EST',6990,8,35,'E3-A',null),
('PAS-POL-CCR-AZU-EST-001','Poleron - CCR - Azul - EST','Parte Superior','Poleron','Con Cierre','','','Azul','EST',6990,8,34,'E3-B',null),
('PAS-POL-SCR-NEG-EST-001','Poleron - SCR - Negro - EST','Parte Superior','Poleron','Sin Cierre','','','Negro','EST',6990,8,32,'E4-A',null),
('PAS-POL-SCR-AZU-EST-001','Poleron - SCR - Azul - EST','Parte Superior','Poleron','Sin Cierre','','','Azul','EST',6990,4,17,'E4-B',null),
('ENT-VST-CR-MC-ROS-EST-001','Vestido - CR-MC - Rosado - EST','Enterizo','Vestido','','Manga Corta','Cuello Redondo','Rosado','EST',4990,4,17,'E11-B',null),
('ENT-VST-CR-MC-VER-EST-001','Vestido - CR-MC - Verde - EST','Enterizo','Vestido','','Manga Corta','Cuello Redondo','Verde','EST',4990,4,17,'E11-B',null),
('ENT-VST-CC-MC-CEL-EST-001','Vestido - CC-MC - Celeste - EST','Enterizo','Vestido','','Manga Corta','Cuello Cuadrado','Celeste','EST',4990,4,17,'E11-A',null),
('ENT-VST-CC-ML-NAR-EST-001','Vestido - CC-ML - Naranja - EST','Enterizo','Vestido','','Manga Larga','Cuello Cuadrado','Naranja','EST',4990,4,17,'E12-A',null),
('ENT-VST-CC-ML-VER-EST-001','Vestido - CC-ML - Verde - EST','Enterizo','Vestido','','Manga Larga','Cuello Cuadrado','Verde','EST',4990,4,16,'E12-A',null),
('PAI-BSH-EST-NAR-EST-001','Biker Shorts - EST - Naranja - EST','Parte Inferior','Biker Shorts','Estandar','','','Naranja','EST',3990,4,17,'E9-C',null),
('PAI-BSH-EST-BUR-EST-001','Biker Shorts - EST - Burdeo - EST','Parte Inferior','Biker Shorts','Estandar','','','Burdeo','EST',3990,4,17,'E9-B',null),
('PAI-CAL-NOR-NEG-EST-001','Calza - NOR - Negro - EST','Parte Inferior','Calza','Normal','','','Negro','EST',3990,4,17,'E8-A',null),
('PAI-CAL-NOR-BUR-EST-001','Calza - NOR - Burdeo - EST','Parte Inferior','Calza','Normal','','','Burdeo','EST',3990,4,17,'E8-B',null),
('PAI-CAL-NOR-ROS-EST-001','Calza - NOR - Rosado - EST','Parte Inferior','Calza','Normal','','','Rosado','EST',3990,4,17,'E8-B',null),
('PAI-CAL-FLR-ROJ-EST-001','Calza - FLR - Rojo - EST','Parte Inferior','Calza','Flare','','','Rojo','EST',3990,4,17,'E8-C',null),
('PAS-BLU-CR-MC-NEG-EST-001','Blusa - CR-MC - Negro - EST','Parte Superior','Blusa','','Manga Corta','Cuello Redondo','Negro','EST',2990,4,17,'E1-A',null),
('PAI-FAL-EST-ROJ-EST-001','Falda - EST - Rojo - EST','Parte Inferior','Falda','Estandar','','','Rojo','EST',2990,4,17,'E10-A',null),
('PAI-FAL-EST-MAR-EST-001','Falda - EST - Marron - EST','Parte Inferior','Falda','Estandar','','','Marron','EST',2990,4,17,'E10-B',null),
('PAI-FAL-EST-BUR-EST-001','Falda - EST - Burdeo - EST','Parte Inferior','Falda','Estandar','','','Burdeo','EST',2990,4,17,'E10-A',null),
('PAS-BLU-CR-MC-ROS-EST-001','Blusa - CR-MC - Rosado - EST','Parte Superior','Blusa','','Manga Corta','Cuello Redondo','Rosado','EST',2990,4,16,'E1-A',null),
('PAS-BLU-CR-ML-BUR-EST-001','Blusa - CR-ML - Burdeo - EST','Parte Superior','Blusa','','Manga Larga','Cuello Redondo','Burdeo','EST',2990,4,16,'E2-A',null),
('PAS-BLU-CR-ML-MAR-EST-001','Blusa - CR-ML - Marron - EST','Parte Superior','Blusa','','Manga Larga','Cuello Redondo','Marron','EST',2990,4,16,'E2-A',null),
('PAS-BLU-CC-MC-AZU-EST-001','Blusa - CC-MC - Azul - EST','Parte Superior','Blusa','','Manga Corta','Cuello Cuadrado','Azul','EST',2990,4,16,'E1-B',null),
('PAS-BLU-CC-MC-GRI-EST-001','Blusa - CC-MC - Gris - EST','Parte Superior','Blusa','','Manga Corta','Cuello Cuadrado','Gris','EST',2990,4,16,'E1-B',null),
('PAS-BLU-CC-ML-NEG-EST-001','Blusa - CC-ML - Negro - EST','Parte Superior','Blusa','','Manga Larga','Cuello Cuadrado','Negro','EST',2990,4,16,'E2-B',null);

insert into public.productos (codigo,nombre,parte_general,tipo,diseno,tipo_manga,tipo_cuello,color,talla,precio,stock_minimo)
select codigo,nombre,parte,tipo,diseno,manga,cuello,color,talla,precio,minimo from carga_inicial;
insert into public.ubicaciones (codigo,estante,seccion)
select distinct codigo, case when codigo like 'E%' then split_part(codigo,'-',1) else null end, case when codigo like '%-%' then split_part(codigo,'-',2) else null end
from (select ubicacion1 as codigo from carga_inicial union select ubicacion2 from carga_inicial where ubicacion2 is not null) u;
insert into public.existencias (producto_id,ubicacion_id,cantidad)
select p.id,u.id,case when c.ubicacion2 is null then c.stock else (c.stock+1)/2 end from carga_inicial c join public.productos p on p.codigo=c.codigo join public.ubicaciones u on u.codigo=c.ubicacion1
union all
select p.id,u.id,c.stock/2 from carga_inicial c join public.productos p on p.codigo=c.codigo join public.ubicaciones u on u.codigo=c.ubicacion2 where c.ubicacion2 is not null;
insert into public.movimientos_inventario (producto_id,ubicacion_id,tipo,cantidad,motivo)
select e.producto_id,e.ubicacion_id,'saldo_inicial',e.cantidad,'Carga inicial desde SeedDataLoader' from public.existencias e where e.cantidad > 0;
commit;
