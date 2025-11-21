package com.example.robles_farma.retrofit;

import com.example.robles_farma.request.CitaCreateRequest;
import com.example.robles_farma.request.EditarUbicacionCitaRequest;
import com.example.robles_farma.request.LoginRequest;
import com.example.robles_farma.request.PacienteUpdatePassRequest;
import com.example.robles_farma.request.PacienteUpdateRequest;
import com.example.robles_farma.request.RegisterRequest;
import com.example.robles_farma.response.BusquedaEspecialidadResponse;
import com.example.robles_farma.response.CancelarCitaResponse;
import com.example.robles_farma.response.CitaResponse;
import com.example.robles_farma.response.CitasPacienteResponse;
import com.example.robles_farma.response.DispositivoUsuarioResponse;
import com.example.robles_farma.response.EditarUbicacionCitaResponse;
import com.example.robles_farma.response.FotoUploadResponse;
import com.example.robles_farma.response.ItemListResponse;
import com.example.robles_farma.response.HorarioEspecialidadResponse;
import com.example.robles_farma.response.ItemResponse;
import com.example.robles_farma.response.LoginResponse;
import com.example.robles_farma.response.MedicoConHorariosResponse;
import com.example.robles_farma.response.PacienteResponse;
import com.example.robles_farma.response.EspecialidadResponse;
import com.example.robles_farma.response.PacienteUpdatePassResponse;
import com.example.robles_farma.request.DispositivoPacienteRequest;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Streaming;
import retrofit2.http.Query;


public interface ApiService {

    // 🔹 Login paciente
    @POST("pacientes/login")
    Call<ItemResponse<LoginResponse>> login(@Body LoginRequest request);

    // 🔹 Registrar paciente
    @POST("pacientes/")
    Call<ItemResponse<PacienteResponse>> registerPaciente(@Body RegisterRequest request);

    @GET("pacientes/")
    Call<ItemListResponse<PacienteResponse>> getPacientes();

    @GET("especialidades/")
    Call<EspecialidadResponse> getEspecialidades(
            @Header("Authorization") String token
    );

    // obtener el top de especialidades
    @GET("especialidades/mas-solicitadas")
    Call<EspecialidadResponse> getEspecialidadesMasSolicitadas(@Header("Authorization") String token);

    @GET("especialidades/{busqueda}")
    Call<BusquedaEspecialidadResponse> getBusquedaEspecialidad(
            @Path("busqueda") String busqueda,
            @Header("Authorization") String token
    );

    @PUT("pacientes/update")
    Call<ItemResponse<PacienteResponse>> updatePaciente(@Body PacienteUpdateRequest request);

    @PUT("pacientes/update_password")
    Call<PacienteUpdatePassResponse> updatePacientePassword(@Body PacienteUpdatePassRequest request);

    // 🔹 Obtener citas proximas de un paciente
    @GET("cita/proximas_paciente/{id_paciente}")
    Call<CitasPacienteResponse> getCitasProximas(@Path("id_paciente") int idPaciente);

    // 🔹 Obtener citas pasadas de un paciente
    @GET("cita/pasadas_paciente/{id_paciente}")
    Call<CitasPacienteResponse> getCitasPasadas(@Path("id_paciente") int idPaciente);

    // 🔹 Cancelar una cita
    @POST("cita/{id_cita}/cancelar")
    Call<CancelarCitaResponse> cancelarCita(@Path("id_cita") int idCita);

    // 🔹 Editar ubicación de cita
    @PATCH("cita/{id_cita}/direccion")
    Call<EditarUbicacionCitaResponse> editarUbicacionCita(
            @Path("id_cita") int idCita,
            @Body EditarUbicacionCitaRequest request
    );


    // 🔹 Obtener horarios disponibles por fecha y especialidad
    @GET("horarios_disponibles/especialidad/{idEspecialidad}")
    Call<HorarioEspecialidadResponse> getHorariosDisponibles(
            @Path("idEspecialidad") int idEspecialidad,
            @Query("fecha") String fecha,
            @Query("en_centro_medico") boolean enCentroMedico,
            @Header("Authorization") String token
    );


    @Streaming
    @GET("pacientes/foto")
    Call<ResponseBody> getFotoPerfil();

    @Multipart
    @PUT("pacientes/update_foto")
    Call<FotoUploadResponse> updateFotoPerfil(@Part MultipartBody.Part file);

    // Registrar el token del paciente
    @POST("dispositivos/crear")
    Call<DispositivoUsuarioResponse> registrarToken(@Body DispositivoPacienteRequest request);

    // Registrar dispositivo paciente
    @POST("pacientes/registrar-token-fcm")
    Call<ResponseBody> registrarDispositivo(@Body DispositivoPacienteRequest request);


    @GET("horarios_disponibles/especialidad/{id_especialidad}")
    Call<ItemListResponse<MedicoConHorariosResponse>> getHorariosPorEspecialidad(
            @Path("id_especialidad") int idEspecialidad,
            @Query("fecha") String fecha,          // Formato YYYY-MM-DD
            @Query("en_centro_medico") boolean enCentroMedico
    );
    // 🔹 Crear una nueva cita
    @POST("cita/")
    Call<ItemResponse<CitaResponse>> createCita(@Body CitaCreateRequest request);
}