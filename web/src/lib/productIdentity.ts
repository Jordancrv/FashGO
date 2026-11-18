const partCodes = { 'Parte Superior': 'PAS', 'Parte Inferior': 'PAI', Enterizo: 'ENT' }
const typeCodes = { Blusa:'BL', Top:'TP', 'Top Asimetrico':'TPA', 'Poleron Estandar':'POL', 'Chaleco Estandar':'CH',
  Jeans:'JNS', Calzas:'CAL', Faldas:'FAL', Shorts:'SHO', 'Biker Shorts':'BSH', Vestido:'VST' }
const designCodes = { Cargo:'CG', Normal:'NM', Flare:'FL', Estándar:'ET', Estandar:'ET' }
const neckCodes = { 'Cuello Redondo':'CR', 'Cuello Cuadrado':'CC', NA:'NA' }
const sleeveCodes = { 'Manga Corta':'MC', 'Manga Larga':'ML', NA:'NA' }

function token(value: string, size: number, codes: Record<string,string> = {}) {
  if (!value) return ''
  return codes[value] ?? value.normalize('NFD').replace(/\p{M}/gu,'').toUpperCase()
    .replace(/[^A-Z0-9]/g,'').slice(0,size).padEnd(size,'X')
}

export function productIdentity(values: Record<string,string>, catalog: readonly {codigo:string}[]) {
  const {parte_general, tipo, diseno, tipo_cuello, tipo_manga, color, talla} = values
  const lower = parte_general === 'Parte Inferior'
  const prefix = [token(parte_general,3,partCodes), token(tipo,3,typeCodes),
    lower ? token(diseno,2,designCodes) : token(tipo_cuello,2,neckCodes),
    lower ? '' : token(tipo_manga,2,sleeveCodes), token(color,3), token(talla,talla.length)]
    .filter(Boolean).join('-')
  let sequence = 0
  for (const p of catalog) {
    const suffix = p.codigo.slice(prefix.length + 1)
    if (p.codigo.startsWith(prefix + '-') && /^\d+$/.test(suffix)) sequence = Math.max(sequence,Number(suffix))
  }
  return {codigo: prefix + '-' + String(sequence + 1).padStart(3,'0'),
    nombre: [tipo, diseno, tipo_cuello, tipo_manga, color, talla && 'Talla ' + talla].filter(v => v && v !== 'NA').join(' - ')}
}
