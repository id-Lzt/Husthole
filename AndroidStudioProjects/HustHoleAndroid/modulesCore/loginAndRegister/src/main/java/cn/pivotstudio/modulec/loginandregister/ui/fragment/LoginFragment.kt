package cn.pivotstudio.modulec.loginandregister.ui.fragment

import android.os.Bundle
import android.text.SpannableString
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import cn.pivotstudio.moduleb.database.MMKVUtil
import cn.pivotstudio.moduleb.libbase.BuildConfig
import cn.pivotstudio.moduleb.libbase.base.ui.fragment.BaseFragment
import cn.pivotstudio.moduleb.libbase.constant.Constant
import cn.pivotstudio.moduleb.libbase.constant.Constant.USER_TOKEN_V2
import cn.pivotstudio.moduleb.libbase.util.ui.EditTextUtil
import cn.pivotstudio.modulec.loginandregister.R
import cn.pivotstudio.modulec.loginandregister.databinding.FragmentLoginBinding
import cn.pivotstudio.modulec.loginandregister.viewmodel.LARViewModel
import com.alibaba.android.arouter.launcher.ARouter

class LoginFragment : BaseFragment() {
    private lateinit var binding: FragmentLoginBinding
    private val viewModel: LARViewModel by activityViewModels()
    private val navController by lazy { findNavController() }
    private lateinit var mmkvUtil: MMKVUtil

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false)
        mmkvUtil = MMKVUtil.getMMKV(context)
        binding.etLoginStudentCode.setText("U201913898")
        binding.etLoginPassword.setText("12345678")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            tvHadNotRegistered.setOnClickListener {
                navController.navigate(R.id.action_loginFragment_to_registerFragment)
            }

            tvForgetPassword.setOnClickListener {
                navController.navigate(R.id.action_loginFragment_to_forgetPasswordFragment)
            }

            btnLogin.setOnClickListener {
                viewModel.login(etLoginStudentCode.text.toString(), etLoginPassword.text.toString())
            }

            //Et与Bt绑定，动态改变button背景
            EditTextUtil.EditTextSize(
                etLoginStudentCode,
                SpannableString(resources.getString(R.string.register_2)), 14
            )
            EditTextUtil.EditTextSize(
                etLoginPassword,
                SpannableString(resources.getString(R.string.register_4)), 14
            )
            EditTextUtil.ButtonReaction(etLoginStudentCode, btnLogin)

        }

        viewModel.apply {
//            loginToken.observe(viewLifecycleOwner) { token ->
//                token?.let {
//                    mmkvUtil.apply {
//                        if (!token.isNullOrBlank()) {
//                            // 取消V1接口，此后所有的v1请求都不会带上Token导致失败
//                            put(Constant.USER_TOKEN, "")
//                        }
//                    }
//                    if (BuildConfig.isRelease) {
//                        ARouter.getInstance().build("/homeScreen/HomeScreenActivity")
//                            .navigation()
//                        requireActivity().finish()
//                    }
//                }
//            }

            lifecycleScope.launchWhenStarted {
                loginTokenV2.collect { token ->
                    token.takeIf { it.isNotBlank() }.let {
                        mmkvUtil.apply {
                            if (token.isNotBlank()) {
                                put(Constant.USER_TOKEN_V2, token)
                                put(Constant.IS_LOGIN, true)
                                if (BuildConfig.isRelease) {
                                    ARouter.getInstance().build("/homeScreen/HomeScreenActivity")
                                        .navigation()
                                    requireActivity().finish()
                                }
                            }
                        }
                    }
                }
            }

            showStudentCodeWarning.observe(viewLifecycleOwner) {
                it?.let {
                    if (it) binding.tvLoginWarn.visibility = View.VISIBLE
                    else {
                        binding.tvLoginWarn.visibility = View.GONE
                        doneShowingWarning()
                    }
                }
            }

            tip.observe(viewLifecycleOwner) {
                it?.let {
                    showMsg(it)
                    viewModel.doneShowingTip()
                }
            }

        }

    }
}