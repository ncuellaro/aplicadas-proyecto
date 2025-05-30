package co.edu.unbosque.proyectoaplicadas.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpSession;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

@Controller
public class MainController {

	@GetMapping("/")
	public String index() {
		return "index";
	}

	@GetMapping("/tutoriales")
	public String tutoriales() {
		return "tutoriales"; // tutoriales.html en templates
	}

	@GetMapping("/tutoriales/arbol")
	public String arbol() {
		return "arbol"; // tutoriales.html en templates
	}

	@GetMapping("/tutoriales/knn")
	public String knn() {
		return "knn"; // tutoriales.html en templates
	}

	@GetMapping("/tutoriales/regresion")
	public String regresion() {
		return "regresion"; // tutoriales.html en templates
	}

	@GetMapping("/subir")
	public String subirArchivo() {
		return "subir"; // subir.html en templates
	}

	@GetMapping("/prueba")
	@ResponseBody
	public String prueba() {
		return "¡La app está corriendo!";
	}

	@PostMapping("/subir/procesar")
	public String procesarArchivo(@RequestParam("file") MultipartFile file, Model model, HttpSession session) {
		if (file.isEmpty()) {
			model.addAttribute("mensaje", "Por favor selecciona un archivo para subir.");
			model.addAttribute("archivoSubido", false);
			return "subir";
		}

		String nombreArchivo = file.getOriginalFilename();
		if (nombreArchivo == null || !(nombreArchivo.endsWith(".csv") || nombreArchivo.endsWith(".xlsx"))) {
			model.addAttribute("mensaje", "Solo se permiten archivos .csv o .xlsx");
			model.addAttribute("archivoSubido", false);
			return "subir";
		}

		try {
			List<List<String>> datos = new ArrayList<>();

			if (nombreArchivo.endsWith(".csv")) {
				// Leer CSV normalmente
				BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()));
				String linea;
				String separador = null;
				while ((linea = br.readLine()) != null) {
					if (separador == null) {
						if (linea.contains(","))
							separador = ",";
						else if (linea.contains("\t"))
							separador = "\t";
						else if (linea.contains(";"))
							separador = ";";
						else
							separador = ","; // por defecto
					}
					String[] partes = linea.split(separador);
					List<String> fila = new ArrayList<>();
					for (String parte : partes) {
						fila.add(parte.trim());
					}
					datos.add(fila);
				}
				br.close();

			} else if (nombreArchivo.endsWith(".xlsx")) {
				// Leer Excel con Apache POI
				Workbook workbook = new XSSFWorkbook(file.getInputStream());
				Sheet sheet = workbook.getSheetAt(0); // Primer hoja
				for (Row row : sheet) {
					List<String> fila = new ArrayList<>();
					for (Cell cell : row) {
						cell.setCellType(CellType.STRING); // Forzar texto para simplificar
						fila.add(cell.getStringCellValue());
					}
					datos.add(fila);
				}
				workbook.close();
			}

			session.setAttribute("datosCSV", datos);
			model.addAttribute("mensaje", "Archivo subido exitosamente: " + nombreArchivo);
			model.addAttribute("archivoSubido", true);

		} catch (Exception e) {
			model.addAttribute("mensaje", "Error al procesar el archivo.");
			model.addAttribute("archivoSubido", false);
			e.printStackTrace();
		}

		return "subir";
	}

	@GetMapping("/subir/prediccion/knn")
	public String mostrarKNN(Model model, HttpSession session) {
		List<List<String>> datos = (List<List<String>>) session.getAttribute("datosCSV");

		if (datos == null || datos.isEmpty()) {
			model.addAttribute("mensaje", "Primero sube un archivo.");
			return "subir";
		}

		model.addAttribute("datosCSV", datos);
		return "knn_resultado"; // Esta será tu nueva vista
	}

	@GetMapping("/subir/prediccion/regresion")
	public String mostrarRegresion(Model model, HttpSession session) {
		List<List<String>> datos = (List<List<String>>) session.getAttribute("datosCSV");

		if (datos == null || datos.isEmpty()) {
			model.addAttribute("mensaje", "Primero sube un archivo.");
			return "subir";
		}

		model.addAttribute("datosCSV", datos);
		return "regresion_resultado"; // Esta será tu nueva vista
	}

	@GetMapping("/subir/prediccion/arbol")
	public String mostrarArbol(Model model, HttpSession session) {
		List<List<String>> datos = (List<List<String>>) session.getAttribute("datosCSV");

		if (datos == null || datos.isEmpty()) {
			model.addAttribute("mensaje", "Primero sube un archivo.");
			return "subir";
		}

		model.addAttribute("datosCSV", datos);
		return "arbol_resultado"; // Esta será tu nueva vista
	}

	@PostMapping("/subir/prediccion/arbol/ejecutar")
	public String ejecutarPrediccionArbol(Model model, HttpSession session) {
		List<List<String>> datos = (List<List<String>>) session.getAttribute("datosCSV");

		if (datos == null || datos.isEmpty()) {
			model.addAttribute("mensaje", "Primero suba un archivo CSV válido.");
			return "subir";
		}

		// Aquí asumimos que datos tiene encabezado en la fila 0
		// Creamos datosPredichos y celdasPredichas solo para las filas de datos (fila 1
		// en adelante)
		List<List<String>> datosPredichos = new ArrayList<>();
		List<List<Boolean>> celdasPredichas = new ArrayList<>();

		// Copiar encabezado directamente
		datosPredichos.add(datos.get(0));
		celdasPredichas.add(new ArrayList<>()); // para encabezado, puede estar vacío o false

		// Procesar filas de datos reales (desde i=1)
		for (int i = 1; i < datos.size(); i++) {
			List<String> fila = datos.get(i);
			List<String> filaNueva = new ArrayList<>();
			List<Boolean> filaMarcados = new ArrayList<>();

			for (String celda : fila) {
				if ("?".equals(celda)) {
					filaNueva.add("Predicho");
					filaMarcados.add(true);
				} else {
					filaNueva.add(celda);
					filaMarcados.add(false);
				}
			}

			datosPredichos.add(filaNueva);
			celdasPredichas.add(filaMarcados);
		}

		model.addAttribute("datosCSV", datos);
		model.addAttribute("datosPredichos", datosPredichos);
		model.addAttribute("celdasPredichas", celdasPredichas);

		// Para pasos, si quieres mostrarlos (ejemplo simple)
		List<String> pasos = List.of("Paso 1: Leer datos del CSV", "Paso 2: Identificar celdas vacías (?)",
				"Paso 3: Reemplazar celdas vacías con 'Predicho'", "Paso 4: Mostrar resultados");
		model.addAttribute("pasos", pasos);

		return "arbol_resultado";
	}

	@PostMapping("/subir/prediccion/regresion/ejecutar")
	public String ejecutarPrediccionRegresion(Model model, HttpSession session) {
		List<List<String>> datos = (List<List<String>>) session.getAttribute("datosCSV");

		if (datos == null || datos.isEmpty()) {
			model.addAttribute("mensaje", "Primero suba un archivo CSV válido.");
			return "subir";
		}

		// Primero, identificamos las filas completas (sin '?') para entrenamiento
		List<List<String>> filasCompletas = new ArrayList<>();
		for (List<String> fila : datos) {
			if (!fila.contains("?")) {
				filasCompletas.add(fila);
			}
		}

		if (filasCompletas.isEmpty()) {
			model.addAttribute("mensaje", "No hay filas completas para entrenar el modelo.");
			return "subir";
		}

		int nFilas = filasCompletas.size();
		int nCols = filasCompletas.get(0).size();

		// Construir matriz X con columna 1 para intercepto, y vector Y (última columna)
		double[][] X = new double[nFilas][nCols]; // +1 para intercepto, pero aquí se pone intercepto en columna 0
		double[] Y = new double[nFilas];

		for (int i = 0; i < nFilas; i++) {
			List<String> fila = filasCompletas.get(i);
			X[i][0] = 1; // Intercepto
			for (int j = 0; j < nCols - 1; j++) {
				String valor = fila.get(j);
				try {
					X[i][j + 1] = Double.parseDouble(valor);
				} catch (NumberFormatException e) {
					// Aquí decides qué hacer si no es un número válido
					// Por ejemplo: mostrar mensaje, asignar un valor por defecto o abortar
					model.addAttribute("mensaje",
							"Error: valor no numérico '" + valor + "' en fila " + i + ", columna " + j);
					return "subir"; // Regresa a la vista para corregir el CSV
				}
			}
			try {
				Y[i] = Double.parseDouble(fila.get(nCols - 1));
			} catch (NumberFormatException e) {
				model.addAttribute("mensaje", "Error: valor no numérico '" + fila.get(nCols - 1) + "' en fila " + i
						+ " (variable dependiente)");
				return "subir";
			}
		}

		// Ajustar tamaño matriz X para intercepto + variables independientes
		// Aquí nCols ya incluye variable dependiente, por eso X tiene columnas nCols
		// (intercepto + nCols-1)
		// No hacemos nada más porque ya se asignó así

		// Calcular coeficientes regresión múltiple
		double[] coeficientes = regresionLinealMultiple(X, Y);

		// Ahora predecir los valores faltantes en datos originales
		List<List<String>> datosPredichos = new ArrayList<>();
		List<List<Boolean>> celdasPredichas = new ArrayList<>();

		for (List<String> fila : datos) {
			List<String> filaNueva = new ArrayList<>();
			List<Boolean> filaMarcados = new ArrayList<>();

			for (int j = 0; j < fila.size(); j++) {
				String celda = fila.get(j);

				if ("?".equals(celda)) {
					// Predecir este valor con regresión lineal múltiple

					// Para predecir necesitamos todas las variables independientes
					// Si faltan otras variables en esta fila (?), no podemos predecir
					// Aquí asumimos que sólo falta 1 valor por fila para simplificar

					// Construimos vector fila para predecir:
					double[] filaParaPredecir = new double[coeficientes.length]; // tamaño coeficientes
					filaParaPredecir[0] = 1; // intercepto

					boolean puedePredecir = true;
					int varIndex = 1; // variable independiente empieza en índice 1

					for (int k = 0; k < fila.size(); k++) {
						if (k == j)
							continue; // esta es la variable que queremos predecir, la dejamos vacía

						String val = fila.get(k);
						if ("?".equals(val)) {
							// No se puede predecir si hay más de una incógnita en la fila
							puedePredecir = false;
							break;
						}

						// Asignar valor numérico en el vector filaParaPredecir
						if (varIndex < filaParaPredecir.length) {
							filaParaPredecir[varIndex] = Double.parseDouble(val);
							varIndex++;
						} else {
							// Esto pasa si hay más columnas que coeficientes (debería coincidir)
							puedePredecir = false;
							break;
						}
					}

					if (puedePredecir && varIndex == filaParaPredecir.length) {
						// Producto punto coeficientes * filaParaPredecir para predecir
						double prediccion = 0;
						for (int c = 0; c < coeficientes.length; c++) {
							prediccion += coeficientes[c] * filaParaPredecir[c];
						}
						filaNueva.add(String.format("%.4f", prediccion));
						filaMarcados.add(true);
					} else {
						// No se puede predecir, dejamos '?'
						filaNueva.add("?");
						filaMarcados.add(false);
					}

				} else {
					filaNueva.add(celda);
					filaMarcados.add(false);
				}
			}
			datosPredichos.add(filaNueva);
			celdasPredichas.add(filaMarcados);
		}

		model.addAttribute("datosCSV", datos);
		model.addAttribute("datosPredichos", datosPredichos);
		model.addAttribute("celdasPredichas", celdasPredichas);
		model.addAttribute("coeficientes", coeficientes);
		double[] betas = regresionLinealMultiple(X, Y);
		model.addAttribute("betas", betas);

		return "regresion_resultado";
	}

	private double[] regresionLinealMultiple(double[][] X, double[] Y) {
		int filas = X.length;
		int cols = X[0].length;

		// 1. Calcular X^T (transpuesta de X)
		double[][] XT = new double[cols][filas];
		for (int i = 0; i < filas; i++) {
			for (int j = 0; j < cols; j++) {
				XT[j][i] = X[i][j];
			}
		}

		// 2. Calcular XT * X
		double[][] XTX = new double[cols][cols];
		for (int i = 0; i < cols; i++) {
			for (int j = 0; j < cols; j++) {
				double suma = 0;
				for (int k = 0; k < filas; k++) {
					suma += XT[i][k] * X[k][j];
				}
				XTX[i][j] = suma;
			}
		}

		// 3. Calcular la inversa de XTX
		double[][] XTXInv = inversaMatriz(XTX);

		// 4. Calcular XT * Y
		double[] XTY = new double[cols];
		for (int i = 0; i < cols; i++) {
			double suma = 0;
			for (int j = 0; j < filas; j++) {
				suma += XT[i][j] * Y[j];
			}
			XTY[i] = suma;
		}

		// 5. Calcular coeficientes = XTXInv * XTY
		double[] beta = new double[cols];
		for (int i = 0; i < cols; i++) {
			double suma = 0;
			for (int j = 0; j < cols; j++) {
				suma += XTXInv[i][j] * XTY[j];
			}
			beta[i] = suma;
		}

		return beta;
	}

	// Método para calcular la inversa de una matriz cuadrada usando el método de
	// Gauss-Jordan
	private double[][] inversaMatriz(double[][] matriz) {
		int n = matriz.length;
		double[][] A = new double[n][n];
		double[][] I = new double[n][n];

		// Copiar matriz original en A y crear matriz identidad I
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				A[i][j] = matriz[i][j];
				I[i][j] = (i == j) ? 1 : 0;
			}
		}

		// Aplicar eliminación Gauss-Jordan
		for (int i = 0; i < n; i++) {
			// Encontrar el pivote
			double pivote = A[i][i];
			if (pivote == 0) {
				throw new RuntimeException("No se puede invertir matriz singular.");
			}
			// Normalizar fila i
			for (int j = 0; j < n; j++) {
				A[i][j] /= pivote;
				I[i][j] /= pivote;
			}

			// Hacer ceros en columna i para otras filas
			for (int k = 0; k < n; k++) {
				if (k != i) {
					double factor = A[k][i];
					for (int j = 0; j < n; j++) {
						A[k][j] -= factor * A[i][j];
						I[k][j] -= factor * I[i][j];
					}
				}
			}
		}
		return I;
	}

	@PostMapping("/subir/prediccion/knn/ejecutar")
	public String ejecutarPrediccionKnn(
			@RequestParam(value = "columnasClustering", required = false) List<Integer> columnasClustering, Model model,
			HttpSession session) {

		List<List<String>> datos = (List<List<String>>) session.getAttribute("datosCSV");

		if (datos == null || datos.isEmpty()) {
			model.addAttribute("mensaje", "Primero suba un archivo CSV válido.");
			return "subir";
		}

		List<List<String>> datosPredichos = new ArrayList<>();
		for (List<String> fila : datos) {
			datosPredichos.add(new ArrayList<>(fila));
		}

		List<List<Boolean>> celdasPredichas = new ArrayList<>();

		// Columnas con valores faltantes
		List<Integer> columnasConFaltantes = new ArrayList<>();
		for (int col = 0; col < datos.get(0).size(); col++) {
			for (int fila = 1; fila < datos.size(); fila++) {
				if ("?".equals(datos.get(fila).get(col))) {
					columnasConFaltantes.add(col);
					break;
				}
			}
		}

		if (columnasClustering == null || columnasClustering.isEmpty()) {
			columnasClustering = new ArrayList<>();
			for (int col = 0; col < datos.get(0).size(); col++) {
				if (!columnasConFaltantes.contains(col)) {
					columnasClustering.add(col);
				}
			}
		}

		// Agrupar por columna "Clase" (índice 1)
		int colCluster = 1; // Suponemos que la clase es el cluster
		Map<String, List<Integer>> clusters = new HashMap<>();
		for (int fila = 1; fila < datos.size(); fila++) {
			String claveCluster = datos.get(fila).get(colCluster);
			if (!"?".equals(claveCluster)) {
				clusters.computeIfAbsent(claveCluster, k -> new ArrayList<>()).add(fila);
			}
		}

		// Calcular los centros por cluster
		Map<String, Map<Integer, String>> centros = new HashMap<>();
		for (String cluster : clusters.keySet()) {
			List<Integer> filas = clusters.get(cluster);
			Map<Integer, String> centro = new HashMap<>();

			for (int col : columnasClustering) {
				boolean esNumerica = true;
				List<Double> valoresNum = new ArrayList<>();
				Map<String, Integer> freq = new HashMap<>();

				for (int fila : filas) {
					String val = datos.get(fila).get(col);
					if (!"?".equals(val)) {
						try {
							valoresNum.add(Double.parseDouble(val));
						} catch (NumberFormatException e) {
							esNumerica = false;
							freq.put(val, freq.getOrDefault(val, 0) + 1);
						}
					}
				}

				if (esNumerica && !valoresNum.isEmpty()) {
					double suma = 0.0;
					for (double v : valoresNum)
						suma += v;
					centro.put(col, String.format("%.2f", suma / valoresNum.size()));
				} else if (!freq.isEmpty()) {
					String moda = freq.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey)
							.orElse("?");
					centro.put(col, moda);
				}
			}

			centros.put(cluster, centro);
		}

		// Predecir valores faltantes
		for (int fila = 1; fila < datos.size(); fila++) {
			String cluster = datos.get(fila).get(colCluster);
			if (!centros.containsKey(cluster))
				continue;

			for (int col : columnasConFaltantes) {
				String val = datos.get(fila).get(col);
				if ("?".equals(val)) {
					String predicho = centros.get(cluster).getOrDefault(col, "?");
					datosPredichos.get(fila).set(col, predicho);
				}
			}
		}

		// Marcar diferencias
		for (int fila = 0; fila < datos.size(); fila++) {
			List<Boolean> filaMarcada = new ArrayList<>();
			for (int col = 0; col < datos.get(fila).size(); col++) {
				filaMarcada.add(!Objects.equals(datos.get(fila).get(col), datosPredichos.get(fila).get(col)));
			}
			celdasPredichas.add(filaMarcada);
		}

		// Preparar info clusters
		List<Map<String, Object>> infoClusters = new ArrayList<>();
		for (String cluster : centros.keySet()) {
			Map<String, Object> info = new HashMap<>();
			info.put("nombre", cluster);
			info.put("centros", centros.get(cluster));
			infoClusters.add(info);
		}

		model.addAttribute("datosCSV", datos);
		model.addAttribute("datosPredichos", datosPredichos);
		model.addAttribute("celdasPredichas", celdasPredichas);
		model.addAttribute("clusters", infoClusters);
		model.addAttribute("columnasClustering", columnasClustering);

		return "knn_resultado";
	}

}
