// Genera una carga revisable desde el inventario existente de Android.
const fs = require('fs');
const source = fs.readFileSync('app/src/main/java/com/example/fashgo/data/database/SeedDataLoader.kt', 'utf8').split('fun loadMateriaPrima')[0];
const lines = source.split(/\r?\n/).filter(line => /^\s*producto\(/.test(line));
const typeCodes = {Blusa:'BLU', Jeans:'JNS', Short:'SHO', Poleron:'POL', Vestido:'VST', 'Biker Shorts':'BSH', Calza:'CAL', Falda:'FAL'};
const colors = {Negro:'NEG', Azul:'AZU', Celeste:'CEL', Rosado:'ROS', Verde:'VER', Naranja:'NAR', Burdeo:'BUR', Rojo:'ROJ', Marron:'MAR', Gris:'GRI'};
const necks = {'Cuello Redondo':'CR', 'Cuello Cuadrado':'CC'};
const sleeves = {'Manga Corta':'MC', 'Manga Larga':'ML'};
const designs = {Cargo:'CG', Denim:'DEN', Estandar:'EST', Normal:'NOR', Flare:'FLR', 'Con Cierre':'CCR', 'Sin Cierre':'SCR'};
const norm = (s,n) => s.normalize('NFD').replace(/[\u0300-\u036f]/g,'').toUpperCase().replace(/[^A-Z0-9]/g,'').slice(0,n).padEnd(n,'X');
const q = s => "'" + String(s).replace(/'/g,"''") + "'";
const rows = lines.map(line => {
  const head = line.match(/producto\("([^"]+)",\s*"([^"]+)"/);
  const fields = Object.fromEntries([...line.matchAll(/(\w+)\s*=\s*(?:"([^"]*)"|(\d+(?:\.\d+)?))/g)].map(m => [m[1],m[2] ?? m[3]]));
  const [tipo, parte] = head.slice(1), diseno = fields.diseno || '', manga = fields.tipoManga || '', cuello = fields.tipoCuello || '';
  const variant = parte === 'Parte Inferior' ? (designs[diseno] || norm(diseno,3)) : cuello ? (necks[cuello] || norm(cuello,2)) : (designs[diseno] || norm(diseno,3));
  const codigo = [({'Parte Superior':'PAS','Parte Inferior':'PAI',Enterizo:'ENT'})[parte], typeCodes[tipo] || norm(tipo,3), variant, sleeves[manga] || '', colors[fields.color] || norm(fields.color,3), fields.talla.toUpperCase(), '001'].filter(Boolean).join('-');
  const detail = cuello || manga ? [necks[cuello] || '', sleeves[manga] || ''].filter(Boolean).join('-') : designs[diseno] || (diseno ? norm(diseno,3) : '');
  const nombre = [tipo,detail,fields.color,fields.talla].filter(Boolean).join(' - ');
  return {codigo,nombre,parte,tipo,diseno,manga,cuello,color:fields.color,talla:fields.talla,precio:Number(fields.precio),stock:Number(fields.stock),ubicaciones:fields.ubicacion.split(' + ')};
});
if(rows.length !== 35 || rows.reduce((a,r)=>a+r.stock,0)!==636 || new Set(rows.map(r=>r.codigo)).size!==35) throw Error('Los datos no coinciden con los 35 productos y 636 unidades esperados.');
const values = rows.map(r => '(' + [r.codigo,r.nombre,r.parte,r.tipo,r.diseno,r.manga,r.cuello,r.color,r.talla].map(q).join(',') + ',' + [r.precio,Math.max(3,Math.floor(r.stock*.25)),r.stock,q(r.ubicaciones[0]),r.ubicaciones[1] ? q(r.ubicaciones[1]) : 'null'].join(',') + ')').join(',\n');
const sql = `-- Generado de SeedDataLoader.kt. Ejecutar solo una vez en una base nueva.\nbegin;\ncreate temporary table carga_inicial (codigo text, nombre text, parte text, tipo text, diseno text, manga text, cuello text, color text, talla text, precio numeric, minimo integer, stock integer, ubicacion1 text, ubicacion2 text) on commit drop;\ninsert into carga_inicial values\n${values};\n\ninsert into public.productos (codigo,nombre,parte_general,tipo,diseno,tipo_manga,tipo_cuello,color,talla,precio,stock_minimo)\nselect codigo,nombre,parte,tipo,diseno,manga,cuello,color,talla,precio,minimo from carga_inicial;\ninsert into public.ubicaciones (codigo,estante,seccion)\nselect distinct codigo, case when codigo like 'E%' then split_part(codigo,'-',1) else null end, case when codigo like '%-%' then split_part(codigo,'-',2) else null end\nfrom (select ubicacion1 as codigo from carga_inicial union select ubicacion2 from carga_inicial where ubicacion2 is not null) u;\ninsert into public.existencias (producto_id,ubicacion_id,cantidad)\nselect p.id,u.id,case when c.ubicacion2 is null then c.stock else (c.stock+1)/2 end from carga_inicial c join public.productos p on p.codigo=c.codigo join public.ubicaciones u on u.codigo=c.ubicacion1\nunion all\nselect p.id,u.id,c.stock/2 from carga_inicial c join public.productos p on p.codigo=c.codigo join public.ubicaciones u on u.codigo=c.ubicacion2 where c.ubicacion2 is not null;\ninsert into public.movimientos_inventario (producto_id,ubicacion_id,tipo,cantidad,motivo)\nselect e.producto_id,e.ubicacion_id,'saldo_inicial',e.cantidad,'Carga inicial desde SeedDataLoader' from public.existencias e where e.cantidad > 0;\ncommit;\n`;
fs.writeFileSync('supabase/seed.sql',sql);
console.log(`Preparados ${rows.length} productos, ${rows.reduce((a,r)=>a+r.stock,0)} unidades; ${rows.filter(r=>r.ubicaciones.length===2).length} repartidos.`);
