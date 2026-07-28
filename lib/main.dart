import 'package:flutter/material.dart';
void main()=>runApp(const AppMedidores());
class AppMedidores extends StatelessWidget {
  const AppMedidores({super.key});
  @override Widget build(BuildContext context) => MaterialApp(
    debugShowCheckedModeBanner: false,
    theme: ThemeData(colorScheme: ColorScheme.fromSeed(seedColor: Colors.teal), useMaterial3: true),
    home: const PantallaPrincipal());
}
class Inquilino {
  final String id, nombre, departamento, medidor;
  final Map<String, Registro> meses;
  Inquilino({required this.id, required this.nombre, required this.departamento, required this.medidor, Map<String, Registro>? meses}) : meses = meses ?? {};
  double anterior(String mes) {
    List<String> k = meses.keys.toList()..sort(); int i = k.indexOf(mes);
    return (i > 0) ? (meses[k[i - 1]]?.actual ?? 0.0) : 0.0;
  }
}
class Registro {
  final double precio, actual, anterior;
  Registro({required this.precio, required this.actual, required this.anterior});
  double get total => (actual - anterior) * precio;
}
class PantallaPrincipal extends StatefulWidget {
  const PantallaPrincipal({super.key});
  @override State<PantallaPrincipal> createState() => _PantallaPrincipalState();
}
class _PantallaPrincipalState extends State<PantallaPrincipal> {
  final List<Inquilino> _inqs = [
    Inquilino(id: '1', nombre: 'Juan Pérez', departamento: 'Apto 101', medidor: 'Medidor N° 45A', meses: {
      '2026-06': Registro(precio: 0.80, anterior: 1000, actual: 1200),
      '2026-07': Registro(precio: 0.95, anterior: 1200, actual: 1350),
    })
  ];
  String _mes = '2026-07'; int _dia = 5;
  final _n = TextEditingController(), _d = TextEditingController(), _m = TextEditingController(), _p = TextEditingController(), _a = TextEditingController();
  @override Widget build(BuildContext context) => Scaffold(
    appBar: AppBar(title: const Text('Medidores (Bs.)', style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold)), backgroundColor: Colors.teal, actions: [IconButton(icon: const Icon(Icons.alarm, color: Colors.white), onPressed: _alarma)]),
    body: Column(children: [
      Container(color: Colors.teal.shade50, padding: const EdgeInsets.all(10), child: Row(mainAxisAlignment: MainAxisAlignment.spaceBetween, children: [
        DropdownButton<String>(value: _mes, items: const [DropdownMenuItem(value: '2026-06', child: Text('Junio 2026')), DropdownMenuItem(value: '2026-07', child: Text('Julio 2026')), DropdownMenuItem(value: '2026-08', child: Text('Agosto 2026'))], onChanged: (v) { if (v != null) setState(() => _mes = v); }),
        Text('🔔 Alarma: Día $_dia', style: const TextStyle(color: Colors.teal, fontWeight: FontWeight.bold))
      ])),
      Expanded(child: ListView.builder(itemCount: _inqs.length, itemBuilder: (c, i) {
        final inq = _inqs[i]; final reg = inq.meses[_mes]; final ant = inq.anterior(_mes);
        return Card(margin: const EdgeInsets.all(8), child: Padding(padding: const EdgeInsets.all(12), child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
          Row(mainAxisAlignment: MainAxisAlignment.spaceBetween, children: [Text(inq.nombre, style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16)), Text(inq.departamento, style: const TextStyle(color: Colors.grey))]),
          Text('Medidor: ${inq.medidor}', style: const TextStyle(fontSize: 12, color: Colors.blueGrey)), const Divider(),
          if (reg == null) ...[
            Text('Lectura Anterior: $ant KW'), const SizedBox(height: 8),
            ElevatedButton(onPressed: () => _calcular(inq, ant), child: const Text('Calcular Mes'))
          ] else ...[
            Text('Ant: ${reg.anterior} KW | Act: ${reg.actual} KW'),
            Text('Consumo: ${(reg.actual - reg.anterior).toStringAsFixed(2)} KW (Tarifa: ${reg.precio} Bs.)'),
            Text('Total: ${reg.total.toStringAsFixed(2)} Bs.', style: const TextStyle(fontWeight: FontWeight.bold, color: Colors.teal, fontSize: 16))
          ]
        ])));
      }))
    ]),
    floatingActionButton: FloatingActionButton(onPressed: _nuevo, backgroundColor: Colors.teal, child: const Icon(Icons.person_add, color: Colors.white)),
  );
  void _calcular(Inquilino inq, double ant) {
    _p.clear(); _a.clear();
    showDialog(context: context, builder: (c) => AlertDialog(title: const Text('Fijar Tarifa y Lectura'), content: Column(mainAxisSize: MainAxisSize.min, children: [Text('Anterior: $ant KW'), TextField(controller: _p, keyboardType: TextInputType.number, decoration: const InputDecoration(labelText: 'Precio KW (Bs.)')), TextField(controller: _a, keyboardType: TextInputType.number, decoration: const InputDecoration(labelText: 'Lectura Actual'))]), actions: [ElevatedButton(onPressed: () {
      final p = double.tryParse(_p.text) ?? 0.0, a = double.tryParse(_a.text) ?? 0.0;
      if (p > 0 && a >= ant) { setState(() { inq.meses[_mes] = Registro(precio: p, anterior: ant, actual: a); }); Navigator.pop(c); }
    }, child: const Text('Procesar'))]));
  }
  void _nuevo() {
    _n.clear(); _d.clear(); _m.clear();
    showDialog(context: context, builder: (c) => AlertDialog(title: const Text('Nuevo Inquilino'), content: Column(mainAxisSize: MainAxisSize.min, children: [TextField(controller: _n, decoration: const InputDecoration(labelText: 'Nombre')), TextField(controller: _d, decoration: const InputDecoration(labelText: 'Dpto / Cuarto')), TextField(controller: _m, decoration: const InputDecoration(labelText: 'Nº Medidor'))]), actions: [ElevatedButton(onPressed: () {
      if (_n.text.isNotEmpty) { setState(() { _inqs.add(Inquilino(id: DateTime.now().toString(), nombre: _n.text, departamento: _d.text, medidor: _m.text)); }); Navigator.pop(c); }
    }, child: const Text('Registrar'))]));
  }
  void _alarma() {
    final ctrl = TextEditingController(text: _dia.toString());
    showDialog(context: context, builder: (c) => AlertDialog(title: const Text('Día de Alarma Mensual'), content: TextField(controller: ctrl, keyboardType: TextInputType.number, decoration: const InputDecoration(labelText: 'Día del mes')), actions: [ElevatedButton(onPressed: () {
      final d = int.tryParse(ctrl.text); if (d != null && d > 0 && d <= 31) { setState(() => _dia = d); Navigator.pop(c); }
    }, child: const Text('Fijar'))]));
  }
}
